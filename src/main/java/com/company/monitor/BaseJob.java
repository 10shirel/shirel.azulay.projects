package com.company.monitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.company.monitor.utils.SendEmail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class BaseJob extends TimerTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(BaseJob.class);


  private static volatile Hashtable<String, String> urlToSha1ConcurrentMap = new Hashtable<>();

  private static final int MAX_THREADS = 5;
  private static final int MAX_WAIT_MINUTES = 10;

  private RemoteFileChecker remoteFileChecker;

  @Autowired
  private SendEmail email;


  @Autowired
  private ApplicationContext appContext;

  public BaseJob() {
  }

  public void run() {

    // urlToSha1ConcurrentMap - This map will not be loaded (from file) in the first time
    loadMapFromFile();

    List<RemoteFile> remoteFiles = loadRemoteFilesList();

    ExecutorService executor = null;
    try {
      executor = Executors.newFixedThreadPool(MAX_THREADS);
      for (RemoteFile remoteFile : remoteFiles) {
        RemoteFileChecker checker = appContext.getBean(RemoteFileChecker.class);
        checker.setRemoteFile(remoteFile);
        executor.execute(checker);
      }
    } finally {
      if (executor != null) {
        executor.shutdown();
      }
    }

    try {
      if (executor.awaitTermination(MAX_WAIT_MINUTES, TimeUnit.MINUTES)) {
        LOGGER.info("The task was completed in time");
        StringBuilder result = new StringBuilder();
        boolean changed = false;
        for (RemoteFile remoteFile : remoteFiles) {
          if (isSha1Changed(remoteFile.getNormalizedURL().toString(), remoteFile.getSha1())) {
            result.append("File: ").append(remoteFile.getNormalizedURL().toString()).append(" was changed\n");
            changed = true;
          }
        }
        populateFileFromMap();
        if (changed) {
          email.sendMail(null, "Monitor status - Files were changed ", result.toString());
        }
      } else {
        LOGGER.error("The task was unable to complete in {} minutes", MAX_WAIT_MINUTES);
      }
    } catch (InterruptedException e) {
      LOGGER.error("The task was interrupted", e);
    } catch (MalformedURLException e) {
      LOGGER.error("Error", e);
    }
  }

  /**
   *
   * @return
   */
  private static void loadMapFromFile() {
    String[] urlSha1 = new String[2];
    String url;
    String sha1;
    try (Scanner fileScanner = new Scanner(new FileReader(Paths.get(Constants.TXT_SHA1_TO_URL_FILE).toFile()))) {
      while (fileScanner.hasNext()) {
        String currentLine = fileScanner.nextLine();

        //in case that this iteration is the first one
        if (currentLine.trim().equalsIgnoreCase(Constants.FIRST_ITERATION)) {
          urlToSha1ConcurrentMap.put(Constants.IS_FIRST_ITERATION_KEY, Constants.IS_FIRST_ITERATION_VALUE_FALSE);
          return;
        }

        //in case of this iteration is the NOT the first one
        else {
              urlSha1 = currentLine.split(Constants.EQUALS);
              if (urlSha1.length == 2) {
                url = currentLine.split(Constants.EQUALS)[0];
                sha1 = currentLine.split(Constants.EQUALS)[1];
                if ((url.startsWith(Constants.URL) && sha1.startsWith(Constants.SHA1) || url.trim().startsWith(Constants.IS_FIRST_ITERATION_KEY))) {
                  urlToSha1ConcurrentMap.put(url.replace(Constants.URL, Constants.EMPTY_STRING), sha1.replace(Constants.SHA1, Constants.EMPTY_STRING));
                } else {
                  LOGGER.warn("Invalid Entry {}", currentLine);
                }
            LOGGER.warn("Invalid Entry - the entry should contain 'key=value' {}", currentLine);
          }
        }
      }
      //this line will not be perform for the first iteration
      urlToSha1ConcurrentMap.put(Constants.IS_FIRST_ITERATION_KEY, Constants.IS_FIRST_ITERATION_VALUE_FALSE);

    } catch (FileNotFoundException e) {
      LOGGER.error("File not found: ", e);
    }
  }


  private static List<RemoteFile> loadRemoteFilesList() {
    List<RemoteFile> result = new ArrayList<>();
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      File file = new File(Constants.TXT_URL_FILE);
      if (file.exists()) {
        RemoteFile[] resultArray = objectMapper.readValue(file, RemoteFile[].class);
        result = new ArrayList<RemoteFile>(Arrays.asList(resultArray));
      }
    } catch (Exception e) {
      LOGGER.error("Error while reading files DB", e);
    }
    return result;
  }

  /**
   *
   * @param url
   * @param currentSha1
   * @return
   */

  private static boolean isSha1Changed(String url, String currentSha1) {

    //Populate the map in the first time for each urls that came from files.txt
    if (urlToSha1ConcurrentMap.get(url) == null) {
      urlToSha1ConcurrentMap.put(url, currentSha1);
      return false;
    }
    else if (urlToSha1ConcurrentMap.get(url) != null && !urlToSha1ConcurrentMap.get(url).equals(currentSha1)) {
      LOGGER.warn("Sha1 for file {} was chagned. Old sha1 is {}.Current sha1 is {}", url, urlToSha1ConcurrentMap.get(url), currentSha1);
      urlToSha1ConcurrentMap.put(url, currentSha1);
      return true;
    }
    return false;
  }


  private static void populateFileFromMap() {

    PrintWriter writer = null;
    try {
      writer = new PrintWriter((Paths.get(Constants.TXT_SHA1_TO_URL_FILE).toFile()), Constants.UTF8);
    } catch (FileNotFoundException e) {
      LOGGER.error("File not found:", e);
    } catch (UnsupportedEncodingException e) {
      LOGGER.error("Unsupported Encoding:", e);

    }
    writer.println(Constants.NOT_FIRST_ITERATION);
    for (Map.Entry<String, String> entry : urlToSha1ConcurrentMap.entrySet()) {
      if(!entry.getKey().equalsIgnoreCase(Constants.IS_FIRST_ITERATION_KEY)){
        writer.println(Constants.URL + entry.getKey() + Constants.EQUALS + Constants.SHA1 + entry.getValue());
      }
    }
    writer.close();

  }

}
