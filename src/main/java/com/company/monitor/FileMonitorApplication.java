package com.company.monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Timer;


@SpringBootApplication
public class FileMonitorApplication {
  private static final Logger LOGGER = LoggerFactory.getLogger(FileMonitorApplication.class);

  private static int minutes;


  public static void main(String[] args) {

          ApplicationContext appContext = SpringApplication.run(FileMonitorApplication.class);

          Timer timer = new Timer();
          BaseJob job = appContext.getBean(BaseJob.class);
          //TODO:timer configurabily
          timer.schedule(job, 0, 100000);
          /*timer.schedule(job, 0, 600000);
          job.run();*/


        }




}
