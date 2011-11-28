package com.all.tracker.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.all.testing.MockInyectRunner;
import com.all.testing.UnderTest;
import com.all.tracker.model.DownloaderMetrics;

@RunWith(MockInyectRunner.class)
public class TestDownloadMetricsController {

	@UnderTest
	private DownloadMetricsController controller;

	@Test
	public void shouldSplitSeveralStringsA() throws Exception {
		String metrics = "IP=189.135.87.196~DATEREGISTERED=01/Sep/2010:00:36:51~DOWNLOADVERSION=All_Alpha_Install_Win_Offline.exe~CODE=200~BYTES=79187180~EXPLORER=\"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.4 (KHTML, like Gecko) Chrome/5.0.375.127 Safari/533.4\"";
		String[] splitMetrics = controller.splitMetrics(metrics, InstallerMetricsController.charset);
		assertTrue(splitMetrics.length == 6);
		assertEquals(splitMetrics[0], "IP=189.135.87.196");
		assertEquals(splitMetrics[1], "DATEREGISTERED=01/Sep/2010:00:36:51");
		assertEquals(splitMetrics[2], "DOWNLOADVERSION=All_Alpha_Install_Win_Offline.exe");
		assertEquals(splitMetrics[3], "CODE=200");
		assertEquals(splitMetrics[4], "BYTES=79187180");
		assertEquals(splitMetrics[5],
				"EXPLORER=\"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.4 (KHTML, like Gecko) Chrome/5.0.375.127 Safari/533.4\"");
	}

	@Test
	public void shouldSplitSeveralStringsB() throws Exception {
		String metrics = "IP=218.186.12.226~DATEREGISTERED=01/Sep/2010:01:01:32~DOWNLOADVERSION=All_Alpha_Install_Mac_Offline.pkg~CODE=302~BYTES=33~EXPLORER=\"Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_0_1 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8A306 Safari/6531.22.7\"";
			String[] splitMetrics = controller.splitMetrics(metrics, InstallerMetricsController.charset);
		assertTrue(splitMetrics.length == 6);
		assertEquals(splitMetrics[0], "IP=218.186.12.226");
		assertEquals(splitMetrics[1], "DATEREGISTERED=01/Sep/2010:01:01:32");
		assertEquals(splitMetrics[2], "DOWNLOADVERSION=All_Alpha_Install_Mac_Offline.pkg");
		assertEquals(splitMetrics[3], "CODE=302");
		assertEquals(splitMetrics[4], "BYTES=33");
		assertEquals(splitMetrics[5], "EXPLORER=\"Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_0_1 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8A306 Safari/6531.22.7\"");
	}

	@Test
	public void shouldCreateDownloaderMetricsFromStream() throws Exception {
		String metrics = "IP=189.204.69.165~DATEREGISTERED=31/Aug/2010:13:07:09~DOWNLOADVERSION=All_Alpha_Install_Win_Online.exe~CODE=302~BYTES=32~EXPLORER=Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.8) Gecko/20100722 Firefox/3.6.8";
		DownloaderMetrics downloaderMetrics = controller.setFeatures(metrics.split(DownloadMetricsController.charset),
				DownloaderMetrics.class);
		assertNotNull(downloaderMetrics);
		assertNotNull(downloaderMetrics.getIP());
		assertNotNull(downloaderMetrics.getDATEREGISTERED());
		assertNotNull(downloaderMetrics.getDOWNLOADVERSION());
		assertNotNull(downloaderMetrics.getCODE());
		assertNotNull(downloaderMetrics.getBYTES());
		assertNotNull(downloaderMetrics.getEXPLORER());
		assertNotNull(downloaderMetrics.getTimestamp());
		assertEquals("189.204.69.165", downloaderMetrics.getIP());
		assertEquals("31/Aug/2010:13:07:09", downloaderMetrics.getDATEREGISTERED());
		assertEquals("All_Alpha_Install_Win_Online.exe", downloaderMetrics.getDOWNLOADVERSION());
		assertEquals("302", downloaderMetrics.getCODE());
		assertEquals("32", downloaderMetrics.getBYTES());
		assertEquals("Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.8) Gecko/20100722 Firefox/3.6.8",
				downloaderMetrics.getEXPLORER());
	}
}
