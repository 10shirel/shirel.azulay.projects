package com.company.monitor;

import com.company.monitor.utils.SendEmail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
/**
 * A bean that is responsible for downloading a single file
 */
public class RemoteFileChecker implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(RemoteFileChecker.class);

  private RemoteFile remoteFile;

  @Autowired
  SendEmail sendEmail;

  public RemoteFileChecker() {
  }

  public RemoteFileChecker(RemoteFile remoteFile) {
    this.setRemoteFile(remoteFile);
  }

  @Override
  public void run() {
    download();
  }

  private void download() {
    URL url = null;
    try {
      url = remoteFile.getNormalizedURL();
      String filePath = url.getFile();
      File file = File.createTempFile("check_", "_" + FilenameUtils.getName(filePath));

      File downloadedFile = download(url, file);
      if (downloadedFile.exists()) {
        String sha1 = getSha1Hash(downloadedFile);
        remoteFile.setSha1(sha1);

        LOGGER.info("got sha1 hash for {} : {}", url, sha1);
        downloadedFile.delete();
      }
    } catch (Exception e) {
      LOGGER.error("Error while downloading: " + url, e);
    }
  }

  public File download(URL url, File dstFile) {

    try (CloseableHttpClient httpclient = HttpClients.custom().setRedirectStrategy(new LaxRedirectStrategy()).build()) {
      HttpGet get = new HttpGet(url.toURI());
      File downloaded = httpclient.execute(get, new FileDownloadResponseHandler(dstFile));
      return downloaded;
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private class FileDownloadResponseHandler implements ResponseHandler<File> {

    private final File target;

    public FileDownloadResponseHandler(File target) {
      this.target = target;
    }

    @Override
    public File handleResponse(HttpResponse response) throws IOException {
      InputStream source = response.getEntity().getContent();
      FileUtils.copyInputStreamToFile(source, this.target);
      return this.target;
    }
  }

  private synchronized String getSha1Hash(File file) {
    String sha1 = "";
    try (FileInputStream fis = new FileInputStream(file)) {
      sha1 = DigestUtils.sha1Hex(fis);
    } catch (IOException e) {
      LOGGER.error("Error while getting sha1 hash", e);
    }

    return sha1;
  }

  public RemoteFile getRemoteFile() {
    return remoteFile;
  }

  public void setRemoteFile(RemoteFile remoteFile) {
    this.remoteFile = remoteFile;
  }



}
