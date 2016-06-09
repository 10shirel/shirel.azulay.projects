package com.company.monitor;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

import static org.junit.Assert.*;

public class RemoteFileTest
{
	@Test
	public void getNormalizedURL_noIp() throws MalformedURLException
	{
		String url = "https://mydomain.com/file.txt";
		String ip = "*";
		RemoteFile remoteFile = new RemoteFile();
		remoteFile.setIp(ip);
		remoteFile.setUrl(url);

		URL result = remoteFile.getNormalizedURL();

		assertEquals(url, result.toString());
	}

	@Test
	public void getNormalizedURL_withIp() throws MalformedURLException
	{
		String url = "https://mydomain.com/file.txt";
		String ip = "1.2.3.4";
		RemoteFile remoteFile = new RemoteFile();
		remoteFile.setIp(ip);
		remoteFile.setUrl(url);

		URL result = remoteFile.getNormalizedURL();

		assertEquals(url.replace("mydomain.com", ip), result.toString());
	}
}
