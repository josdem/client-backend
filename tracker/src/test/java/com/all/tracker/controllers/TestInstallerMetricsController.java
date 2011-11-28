package com.all.tracker.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.all.testing.MockInyectRunner;
import com.all.testing.UnderTest;
import com.all.tracker.model.InstallerMetrics;

@RunWith(MockInyectRunner.class)
public class TestInstallerMetricsController {

	@UnderTest
	private InstallerMetricsController controller;
	
	@Test
	public void shouldSplitOnTwoStrings() throws Exception {
		String metrics = "test~metrics";
		String[] splitMetrics = controller.splitMetrics(metrics, InstallerMetricsController.charset);
		assertTrue(splitMetrics.length == 2);
		assertEquals(splitMetrics[0], "test");
		assertEquals(splitMetrics[1], "metrics");
	}

	@Test
	public void shouldSplitSeveralStrings() throws Exception {
		String metrics = "STATUS=START~CLIENTVERSION=10.1.1.1.1~INSTALLERTYPE=online~MAC=01-02-03-04-05~OS=MacOSX~OSVERSION=10.6.4~OSLANGUAGE=es~NUMCPU=1~CPUARCHITECTURE=64-Bit(Intel):Yes~CPUMODEL=IntelCore2Duo-2core~RAM=~HD=^/dev/disk0s2-46G^/dev/disk0s3-68G^~JAVAVERSION=1.6.0_20~NETWORKADAPTER=^Atheros AR9285 Wireless Network Adapter-90:4C:E5:E5:0F:1D-192.168.1.47^VMware Virtual Ethernet Adapter for VMnet1-00:50:56:C0:00:01-192.168.169.1^VMware Virtual Ethernet Adapter for VMnet8-00:50:56:C0:00:08-192.168.128";
		String[] splitMetrics = controller.splitMetrics(metrics, InstallerMetricsController.charset);
		assertTrue(splitMetrics.length == 14);
		assertEquals(splitMetrics[0], "STATUS=START");
		assertEquals(splitMetrics[1], "CLIENTVERSION=10.1.1.1.1");
		assertEquals(splitMetrics[2], "INSTALLERTYPE=online");
		assertEquals(splitMetrics[3], "MAC=01-02-03-04-05");
		assertEquals(splitMetrics[4], "OS=MacOSX");
		assertEquals(splitMetrics[7], "NUMCPU=1");
		assertEquals(splitMetrics[10], "RAM=");
	}
	
	@Test
	public void shouldSplitTwice() throws Exception {
		String metrics = "OS=MacOSX~OSVERSION=10.6.4~CLIENTVERSION=10.1.1.1.1~OSLANGUAGE=es~NUMCPU=1~CPUARCHITECTURE=64-Bit(Intel):Yes~CPUMODEL=IntelCore2Duo-2core~RAM=~HD=^/dev/disk0s2-46G^/dev/disk0s3-68G^~JAVAVERSION=1.6.0_20~NETWORKADAPTER=^Atheros AR9285 Wireless Network Adapter-90:4C:E5:E5:0F:1D-192.168.1.47^VMware Virtual Ethernet Adapter for VMnet1-00:50:56:C0:00:01-192.168.169.1^VMware Virtual Ethernet Adapter for VMnet8-00:50:56:C0:00:08-192.168.128~STATUS=START~MAC=01-02-03-04-05~INSTALLERTYPE=offline";
		String[] splitMetrics = controller.splitMetrics(metrics, InstallerMetricsController.charset);
		assertTrue(splitMetrics.length == 14);
		String[] feature = controller.splitMetrics(splitMetrics[0], InstallerMetricsController.charset_inside);
		assertEquals(feature[0], "OS");
		assertEquals(feature[1], "MacOSX");
		feature = splitMetrics[2].split("=");
		assertEquals(feature[0], "CLIENTVERSION");
		assertEquals(feature[1], "10.1.1.1.1");
		feature = splitMetrics[5].split("=");
		assertEquals(feature[0], "CPUARCHITECTURE");
		assertEquals(feature[1], "64-Bit(Intel):Yes");
		feature = splitMetrics[9].split("=");
		assertEquals(feature[0], "JAVAVERSION");
		assertEquals(feature[1], "1.6.0_20");
		feature = splitMetrics[11].split("=");
		assertEquals(feature[0], "STATUS");
		assertEquals(feature[1], "START");
		feature = splitMetrics[12].split("=");
		assertEquals(feature[0], "MAC");
		assertEquals(feature[1], "01-02-03-04-05");
		feature = splitMetrics[13].split("=");
		assertEquals(feature[0], "INSTALLERTYPE");
		assertEquals(feature[1], "offline");
	}
	
	@Test
	public void shouldCreateInstallerMetricsFromStream() throws Exception {
		String metrics = "OS=MacOSX~OSVERSION=10.6.4~CLIENTVERSION=10.1.1.1.1~OSLANGUAGE=es~NUMCPU=1~CPUARCHITECTURE=64-Bit(Intel):Yes~CPUMODEL=IntelCore2Duo-2core~RAM=5~HD=^/dev/disk0s2-46G^/dev/disk0s3-68G^~JAVAVERSION=1.6.0_20~NETWORKADAPTER=^Atheros AR9285 Wireless Network Adapter-90:4C:E5:E5:0F:1D-192.168.1.47^VMware Virtual Ethernet Adapter for VMnet1-00:50:56:C0:00:01-192.168.169.1^VMware Virtual Ethernet Adapter for VMnet8-00:50:56:C0:00:08-192.168.128~STATUS=START~MAC=01-02-03-04-05~INSTALLERTYPE=offline";
		InstallerMetrics installerMetrics = controller.setFeatures(metrics.split(InstallerMetricsController.charset), InstallerMetrics.class);
		assertNotNull(installerMetrics);
		assertNotNull(installerMetrics.getOS());
		assertNotNull(installerMetrics.getOSVERSION());
		assertNotNull(installerMetrics.getOSLANGUAGE());
		assertNotNull(installerMetrics.getNUMCPU());
		assertNotNull(installerMetrics.getCPUARCHITECTURE());
		assertNotNull(installerMetrics.getCPUMODEL());
		assertNotNull(installerMetrics.getHD());
		assertNotNull(installerMetrics.getRAM());
		assertNotNull(installerMetrics.getJAVAVERSION());
		assertNotNull(installerMetrics.getNETWORKADAPTER());
		assertNotNull(installerMetrics.getSTATUS());
		assertNotNull(installerMetrics.getMAC());
		assertNotNull(installerMetrics.getINSTALLERTYPE());
		assertNotNull(installerMetrics.getCLIENTVERSION());
		assertEquals("MacOSX",installerMetrics.getOS());
		assertEquals("10.6.4",installerMetrics.getOSVERSION());
		assertEquals("es",installerMetrics.getOSLANGUAGE());
		assertEquals("1",installerMetrics.getNUMCPU());
		assertEquals("64-Bit(Intel):Yes",installerMetrics.getCPUARCHITECTURE());
		assertEquals("IntelCore2Duo-2core",installerMetrics.getCPUMODEL());
		assertEquals("^/dev/disk0s2-46G^/dev/disk0s3-68G^",installerMetrics.getHD());
		assertEquals("5",installerMetrics.getRAM());
		assertEquals("1.6.0_20",installerMetrics.getJAVAVERSION());
		assertEquals("^Atheros AR9285 Wireless Network Adapter-90:4C:E5:E5:0F:1D-192.168.1.47^VMware Virtual Ethernet Adapter for VMnet1-00:50:56:C0:00:01-192.168.169.1^VMware Virtual Ethernet Adapter for VMnet8-00:50:56:C0:00:08-192.168.128",installerMetrics.getNETWORKADAPTER());
		assertEquals("START",installerMetrics.getSTATUS());
		assertEquals("01-02-03-04-05",installerMetrics.getMAC());
		assertEquals("offline", installerMetrics.getINSTALLERTYPE());
		assertEquals("10.1.1.1.1", installerMetrics.getCLIENTVERSION());
	}
}
