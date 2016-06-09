package com.company.monitor;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.util.StringUtils;

public class RemoteFile
{
  private String url;
  private String sha1;
  private String ip;

  public RemoteFile() {
  }

  public RemoteFile(String url, String ip) {
    this.ip = ip;
    this.url = url;
  }

  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public String getIp()
  {
    return ip;
  }

  public void setIp(String ip)
  {
    this.ip = ip;
  }

  public URL getNormalizedURL() throws MalformedURLException
  {
    URL result = new URL(url);
    if (StringUtils.hasText(ip) && InetAddressValidator.getInstance().isValid(ip))
    {
      result = new URL(result.getProtocol(), ip, result.getPort(), result.getFile());
    }
    return result;
  }

  public String getSha1()
  {
    return sha1 == null ? "" : sha1;
  }

  public void setSha1(String sha1)
  {
    this.sha1 = sha1;
  }

  public void deleteLocalFile()
  {

  }
}
