package com.company.monitor.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@Configuration
@PropertySource("application.properties")
public class EmailConfiguration {

  @Value("${toAdress}")
  protected String toAdress;

  @Value("${fromAdress}")
  protected String fromAdress;

  @Value("${userName}")
  protected String userName;

  @Value("${password}")
  protected String password;

  @Value("${mail.smtp.auth}")
  protected String mailSmtpAuth;

  @Value("${mail.smtp.starttls.enable}")
  protected String mailSmtpStarttlsEnabled;

  @Value("${mail.smtp.host}")
  protected String mailSmtpHost;

  @Value("${mail.smtp.port}")
  protected String mailSmtpPort;

  public String getToAdress() {
    return toAdress;
  }

  public String getFromAdress() {
    return fromAdress;
  }

  public String getUserName() {
    return userName;
  }

  public String getPassword() {
    return password;
  }

  public String getMailSmtpAuth() {
    return mailSmtpAuth;
  }

  public String getMailSmtpStarttlsEnabled() {
    return mailSmtpStarttlsEnabled;
  }

  public String getMailSmtpHost() {
    return mailSmtpHost;
  }

  public String getMailSmtpPort() {
    return mailSmtpPort;
  }
}
