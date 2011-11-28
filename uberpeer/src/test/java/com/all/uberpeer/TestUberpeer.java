package com.all.uberpeer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IoSession;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.all.dht.DhtManager;
import com.all.messengine.impl.StubMessEngine;
import com.all.networking.NetworkingSocketFactory;

public class TestUberpeer {

	@InjectMocks
	private Uberpeer uberpeer = new Uberpeer();
	@Mock
	private DhtManager dhtManager;
	@Spy
	private StubMessEngine messEngine = new StubMessEngine();
	@Mock
	private NetworkingSocketFactory socketConfigurator;
	@Mock
	private IoAcceptor acceptor;
	@Mock
	private Properties dhtConfig;

	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldStart() throws Exception {
		startUberpeer();

		verify(dhtManager).start();
		verify(acceptor).setHandler(uberpeer);
		verify(acceptor).bind(any(InetSocketAddress.class));
		assertNotNull(messEngine.getMessage(UberpeerConstants.START_UBERPEER_SERVICES_TYPE));
	}


	private void startUberpeer() {
		when(socketConfigurator.newAcceptor()).thenReturn(acceptor);
		uberpeer.start();
	}

	
	@SuppressWarnings("unchecked")
	@Test
	public void shouldStop() throws Exception {
		startUberpeer();
		when(acceptor.isActive()).thenReturn(true);
		Map<Long, IoSession> sessions = mock(Map.class);
		IoSession session = mock(IoSession.class);
		when(sessions.values()).thenReturn(Arrays.asList(session));
		when(acceptor.getManagedSessions()).thenReturn(sessions );

		uberpeer.stop();
		
		assertNotNull(messEngine.getMessage(UberpeerConstants.STOP_UBERPEER_SERVICES_TYPE));
		verify(dhtManager).stop();
		verify(acceptor).unbind();
		verify(acceptor).dispose();
		
	}
	
	@Test
	public void shouldLoadSpringContextSuccesfully() throws Exception {
		ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				new String[] { "/applicationContext.xml" });
		applicationContext.close();
	}

	@Test
	public void shouldGetProperty() throws Exception {
		String property = "prop";
		String value = "value";
		when(dhtConfig.getProperty(property)).thenReturn(value);
		
		assertEquals(value, uberpeer.getProperty(property));
	}
	
	@Test
	public void shouldSetProperty() throws Exception {
		String property = "prop";
		String value = "value";
		
		uberpeer.setProperty(property, value);
		
		verify(dhtConfig).setProperty(property, value);
		
	}
	
}
