package com.all.ultrapeer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TestUltraPeerConfig {

	@InjectMocks
	private UltrapeerConfig ultrapeerConfig = new UltrapeerConfig();
	@Mock
	private Properties ultrapeerSettings;
	@Mock
	private Properties dhtConfig;
	@SuppressWarnings("unchecked")
	@Mock
	private Enumeration ultrapeerProps;
	@SuppressWarnings("unchecked")
	@Mock
	private Enumeration dhtProps;

	private String ultrapeerProp = "ultrapeerProp";
	private String ultrapeerPropValue = "ultrapeerPropValue";

	private String dhtProp = "dhtProp";
	private String dhtPropValue = "dhtPropValue";

	@Before
	public void createPeerProfile() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldGetPublicIp() throws Exception {
		String expectedAddress = "192.168.1.103";
		System.setProperty("PUBLIC_IP", expectedAddress);

		InetAddress publicIp = ultrapeerConfig.getPublicIp();

		assertNotNull(publicIp);
		assertTrue(publicIp.getHostAddress().contains(expectedAddress));
	}

	@Test
	public void shouldReturnNullIfNoPublicIp() throws Exception {
		String expectedAddress = "some invalid name";
		System.setProperty("PUBLIC_IP", expectedAddress);

		InetAddress publicIp = ultrapeerConfig.getPublicIp();

		assertNull(publicIp);
	}

	@Test
	public void shouldGetName() throws Exception {
		assertNotNull(ultrapeerConfig.getName());
	}

	@Test
	public void shouldLoadProperties() throws Exception {
		loadProperties();

		assertEquals(dhtPropValue, ultrapeerConfig.getProperty(dhtProp));
		assertEquals(ultrapeerPropValue, ultrapeerConfig.getProperty(ultrapeerProp));
	}

	@SuppressWarnings("unchecked")
	private void loadProperties() {
		when(dhtConfig.propertyNames()).thenReturn(dhtProps);
		when(dhtProps.hasMoreElements()).thenReturn(true, false);
		when(dhtProps.nextElement()).thenReturn(dhtProp);
		when(dhtConfig.getProperty(dhtProp)).thenReturn(dhtPropValue);
		when(ultrapeerSettings.propertyNames()).thenReturn(ultrapeerProps);
		when(ultrapeerProps.hasMoreElements()).thenReturn(true, false);
		when(ultrapeerProps.nextElement()).thenReturn(ultrapeerProp);
		when(ultrapeerSettings.getProperty(ultrapeerProp)).thenReturn(ultrapeerPropValue);

		ultrapeerConfig.initialize();
	}

	@SuppressWarnings("unchecked")
	@Test(expected = IllegalStateException.class)
	public void shouldFailIfDuplicatedProperties() throws Exception {
		when(dhtConfig.propertyNames()).thenReturn(dhtProps);
		when(dhtProps.hasMoreElements()).thenReturn(true, false);
		when(dhtProps.nextElement()).thenReturn(dhtProp);
		when(dhtConfig.getProperty(dhtProp)).thenReturn(dhtPropValue);
		when(ultrapeerSettings.propertyNames()).thenReturn(ultrapeerProps);
		when(ultrapeerProps.hasMoreElements()).thenReturn(true, false);
		when(ultrapeerProps.nextElement()).thenReturn(dhtProp);
		when(ultrapeerSettings.getProperty(ultrapeerProp)).thenReturn(ultrapeerPropValue);

		ultrapeerConfig.initialize();

	}

	@Test
	public void shouldSetProperty() throws Exception {
		loadProperties();

		String ultrapeerPropNewValue = "new value";

		assertEquals(ultrapeerPropValue, ultrapeerConfig.getProperty(ultrapeerProp));
		ultrapeerConfig.setProperty(ultrapeerProp, ultrapeerPropNewValue);

		verify(ultrapeerSettings).setProperty(ultrapeerProp, ultrapeerPropNewValue);
	}
}
