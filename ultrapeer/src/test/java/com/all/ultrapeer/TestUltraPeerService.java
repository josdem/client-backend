package com.all.ultrapeer;

import static com.all.networking.NetworkingConstants.NETWORKING_SESSION_ID;
import static com.all.dht.DhtConstants.CURRENT_ULTRAPEERS_SET_REQUEST_TYPE;
import static com.all.dht.DhtConstants.CURRENT_ULTRAPEERS_SET_RESPONSE_TYPE;
import static com.all.ultrapeer.messages.UltrapeerMessages.Types.USER_PRESENCE_EXPIRED_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IoSession;
import org.bouncycastle.util.encoders.Base64;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.context.ConfigurableApplicationContext;

import com.all.dht.DhtManager;
import com.all.messengine.Message;
import com.all.messengine.impl.StubMessEngine;
import com.all.networking.NetworkingConstants;
import com.all.networking.NetworkingMessage;
import com.all.networking.NetworkingSocketFactory;
import com.all.services.ServiceInvoker;
import com.all.shared.json.JsonConverter;
import com.all.shared.messages.MessEngineConstants;
import com.all.shared.model.AllMessage;
import com.all.shared.model.UltrapeerNode;
import com.all.shared.model.UltrapeerSessionResponse;

public class TestUltraPeerService {
	private static final int TOTAL_ULTRAPEER_MESSAGES = 2;

	@InjectMocks
	private UltrapeerService service = new UltrapeerService();

	@Spy
	private StubMessEngine stubEngine = new StubMessEngine();

	@Mock
	private DhtManager dhtManager;
	@Mock
	private NetworkingSocketFactory socketConfigurator;
	@Mock
	private UltrapeerConfig ultrapeerConfig;
	@Mock
	private UltrapeerMonitor ultrapeerMonitor;

	private long sessionId = 1L;

	private InetSocketAddress remoteAddress = new InetSocketAddress("127.0.0.1", 0);

	@Mock
	private IoSession session;
	private Long peerSessionId = 1L;
	private IoAcceptor ioAcceptor = mock(IoAcceptor.class);
	private DefaultIoFilterChainBuilder filterChain = mock(DefaultIoFilterChainBuilder.class);

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		stubEngine.setup(service);

		when(session.getId()).thenReturn(peerSessionId);
		when(socketConfigurator.newAcceptor()).thenReturn(ioAcceptor);
		when(ioAcceptor.getFilterChain()).thenReturn(filterChain);
		when(ultrapeerConfig.getName()).thenReturn("ultrapeer name");
		when(session.getRemoteAddress()).thenReturn(remoteAddress);
		when(session.getId()).thenReturn(sessionId);
		Long delay = 5l;
		when(ultrapeerConfig.getTypedProperty(anyString(), eq(Long.class))).thenReturn(delay);
	}

	@Test
	public void shouldStart() throws Exception {
		when(ioAcceptor.isActive()).thenReturn(true);
		assertFalse(service.isStarted());
		service.start();
		assertTrue(service.isStarted());

		verify(ioAcceptor).setHandler(service);
		verify(ioAcceptor).bind((SocketAddress) anyObject());
		verify(dhtManager).start();
		Set<String> registeredTypes = stubEngine.getRegisteredTypes();
		assertEquals(UltrapeerService.getRegisteredTypes().size() + TOTAL_ULTRAPEER_MESSAGES, registeredTypes.size());
		assertTrue(registeredTypes.containsAll(UltrapeerService.getRegisteredTypes()));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldStop() throws Exception {
		shouldStart();
		Map<Long, IoSession> sessions = mock(Map.class);
		when(sessions.values()).thenReturn(Arrays.asList(session));
		when(ioAcceptor.getManagedSessions()).thenReturn(sessions);
		service.stop();
		verify(session).close(true);
		verify(ioAcceptor).unbind();
		verify(ioAcceptor).dispose();
		verify(dhtManager).stop();
	}

	@Test
	public void shouldNotStartIfSomethingGoesBad() throws Exception {
		doThrow(new IllegalArgumentException()).when(ioAcceptor).bind((SocketAddress) anyObject());
		when(ioAcceptor.isActive()).thenReturn(false);
		service.start();
		assertFalse(service.isStarted());
	}

	@Test
	public void shouldNotStartTwice() throws Exception {
		shouldStart();
		service.start();
		verify(ioAcceptor, times(1)).bind((SocketAddress) anyObject());
		verify(dhtManager, times(1)).start();
		Set<String> registeredTypes = stubEngine.getRegisteredTypes();
		assertEquals(UltrapeerService.getRegisteredTypes().size() + TOTAL_ULTRAPEER_MESSAGES, registeredTypes.size());
		assertTrue(registeredTypes.containsAll(UltrapeerService.getRegisteredTypes()));
	}

	@Test
	public void shouldSendBackMessageOnDemand() throws Exception {
		service.start();

		service.sessionUpdated(session, new NetworkingMessage("sender", null));
		ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
		AllMessage<?> message = new AllMessage<String>(UltrapeerService.getRegisteredTypes().get(0), "test message");
		message.putProperty(NETWORKING_SESSION_ID, peerSessionId.toString());
		WriteFuture writeFuture = mock(WriteFuture.class);
		when(session.write(anyString())).thenReturn(writeFuture);
		when(writeFuture.isWritten()).thenReturn(true);

		stubEngine.send(message);

		verify(session).write(jsonCaptor.capture());
		verify(writeFuture).await();
		String json = jsonCaptor.getValue();
		NetworkingMessage sentMessage = JsonConverter.toBean(new String(Base64.decode(json.getBytes())),
				NetworkingMessage.class);
		assertNotNull(sentMessage);
		assertEquals(message.getBody(), sentMessage.getBody().getBody());
	}

	@Test
	public void shouldForwardAMessageToADifferentSessionIfItHasTheForwardToProperty() throws Exception {
		service.start();

		IoSession otherPeerSession = mock(IoSession.class);
		String otherUserMail = "otherUser@mail";

		service.sessionUpdated(otherPeerSession, new NetworkingMessage(otherUserMail,
				new AllMessage<String>("type", "body")));
		;

		ArgumentCaptor<byte[]> networkingMessagesCaptor = ArgumentCaptor.forClass(byte[].class);
		Message<?> message = new AllMessage<String>(UltrapeerService.getRegisteredTypes().get(0), "test message");
		message.putProperty(NETWORKING_SESSION_ID, peerSessionId.toString());
		message.putProperty(MessEngineConstants.PROP_FORWARD_TO, otherUserMail);

		stubEngine.send(message);

		verify(otherPeerSession, times(1)).write(networkingMessagesCaptor.capture());
		assertNotNull(networkingMessagesCaptor.getValue());
		verify(session, never()).write(anyObject());
	}

	@Test
	public void shouldRunOnInteractiveMode() throws Exception {
		ConfigurableApplicationContext applicationContext = mock(ConfigurableApplicationContext.class);
		UltrapeerService ultraPeerService = mock(UltrapeerService.class);
		ServiceInvoker invoker = mock(ServiceInvoker.class);
		BufferedReader in = mock(BufferedReader.class);
		when(in.readLine()).thenReturn("start", " ", "invalid command", "invoke invalidProp", "invoke validProp", "stop",
				"shutdown");
		when(invoker.invoke("invalidProp")).thenThrow(new RuntimeException("Some exception"));

		UltrapeerService.runInteractiveMode(applicationContext, ultraPeerService, invoker, in);

		verify(ultraPeerService).start();
		verify(invoker).invoke("validProp");
		verify(ultraPeerService).stop();
		verify(applicationContext).close();
	}

	@Test
	public void shouldHandleMessageReceivedAndIncludeNetworkingPropertiesToTheActualMessage() throws Exception {
		String sender = "email";
		AllMessage<?> message = new AllMessage<String>("some peer request", "test");
		NetworkingMessage networkingMessage = new NetworkingMessage(sender, message);

		Message<?> currentMessage = stubEngine.getCurrentMessage();
		assertNull(currentMessage);

		service.messageReceived(session, new String(Base64.encode(JsonConverter.toJson(networkingMessage).getBytes())));
		currentMessage = stubEngine.getCurrentMessage();
		assertNotNull(currentMessage);
		assertEquals(message.getType(), currentMessage.getType());
		assertEquals(sender, currentMessage.getProperty(NetworkingConstants.MESSAGE_SENDER));
		assertEquals(remoteAddress.getAddress().getHostAddress(), currentMessage
				.getProperty(NetworkingConstants.MESSAGE_SENDER_PUBLIC_ADDRESS));
		assertEquals(sender, currentMessage.getProperty(NetworkingConstants.MESSAGE_SENDER));
		assertNotNull(currentMessage.getProperty(NetworkingConstants.MESSAGE_SENDER));
	}

	@Test
	public void shouldAddAndRemoveSessionsWhenCreatedOrClosed() throws Exception {
		service.sessionCreated(session);
		String sender = "sender";
		NetworkingMessage networkingMessage = new NetworkingMessage(sender, new AllMessage<String>("type", "body"));
		service.sessionUpdated(session, networkingMessage);

		assertEquals(session, service.getSession(sessionId));
		assertEquals(session, service.getSession(sender));

		service.sessionClosed(session);

		assertNull(service.getSession(sessionId));
		assertNull(service.getSession(sender));
		assertNotNull(stubEngine.getMessage(USER_PRESENCE_EXPIRED_TYPE));
	}

	@Test
	public void shouldAddAliasWhenAMessageIsReceivedAndContainsSenderData() throws Exception {
		String senderData = "senderData";
		AllMessage<?> message = new AllMessage<String>("some peer request", "test");
		NetworkingMessage networkingMessage = new NetworkingMessage(senderData, message);

		service.messageReceived(session, new String(Base64.encode(JsonConverter.toJson(networkingMessage).getBytes())));
		assertEquals(session, service.getSession(senderData));
	}

	@Test(timeout = 1000)
	public void shouldRequestTheCurrentUltrapeersListAfterStarting() throws Exception {
		service.start();
		Message<?> request = null;
		do {
			Thread.sleep(50);
			request = stubEngine.getMessage(CURRENT_ULTRAPEERS_SET_REQUEST_TYPE);
		} while (request == null);
	}

	@Test
	public void shouldRespondToUltrapeerSessionRequestsAndUpdateClientUltrapeerList() throws Exception {
		UltrapeerNode ultrapeerA = new UltrapeerNode("192.168.1.1");
		UltrapeerNode ultrapeerB = new UltrapeerNode("192.168.1.2");
		UltrapeerNode ultrapeerC = new UltrapeerNode("192.168.1.3");
		UltrapeerNode ultrapeerD = new UltrapeerNode("192.168.1.4");
		ArrayList<UltrapeerNode> clientList = new ArrayList<UltrapeerNode>();
		clientList.add(ultrapeerA);
		clientList.add(ultrapeerB);
		clientList.add(ultrapeerC);
		HashSet<UltrapeerNode> ultrapeers = new HashSet<UltrapeerNode>();
		ultrapeers.add(ultrapeerA);
		ultrapeers.add(ultrapeerC);
		ultrapeers.add(ultrapeerD);
		Message<?> ultrapeerListMessage = new AllMessage<HashSet<UltrapeerNode>>(CURRENT_ULTRAPEERS_SET_RESPONSE_TYPE,
				ultrapeers);
		stubEngine.addTypeResponse(CURRENT_ULTRAPEERS_SET_REQUEST_TYPE, ultrapeerListMessage);
		shouldRequestTheCurrentUltrapeersListAfterStarting();
		Message<?> requestMessage = new AllMessage<ArrayList<UltrapeerNode>>(
				MessEngineConstants.ULTRAPEER_SESSION_REQUEST_TYPE, clientList);
		when(ultrapeerMonitor.isHealthy()).thenReturn(true);
		
		stubEngine.send(requestMessage);

		Message<?> responseMessage = stubEngine.getMessage(MessEngineConstants.ULTRAPEER_SESSION_RESPONSE_TYPE);
		assertNotNull(responseMessage);
		UltrapeerSessionResponse response = (UltrapeerSessionResponse) responseMessage.getBody();
		assertTrue(response.isAccepted());
		List<UltrapeerNode> newUltrapeers = response.getNewUltrapeers();
		assertNotNull(newUltrapeers);
		assertEquals(1, newUltrapeers.size());
		assertTrue(newUltrapeers.contains(ultrapeerD));
		List<UltrapeerNode> deprecatedUltrapeers = response.getDeprecatedUltrapeers();
		assertNotNull(deprecatedUltrapeers);
		assertEquals(1, deprecatedUltrapeers.size());
		assertTrue(deprecatedUltrapeers.contains(ultrapeerB));
	}

	@Test
	public void shouldAcceptSessionIfHealthy() throws Exception {
		Message<?> requestMessage = new AllMessage<ArrayList<UltrapeerNode>>(
				MessEngineConstants.ULTRAPEER_SESSION_REQUEST_TYPE, new ArrayList<UltrapeerNode>());
		when(ultrapeerMonitor.isHealthy()).thenReturn(true);

		stubEngine.send(requestMessage);

		Message<?> responseMessage = stubEngine.getMessage(MessEngineConstants.ULTRAPEER_SESSION_RESPONSE_TYPE);
		assertNotNull(responseMessage);
		UltrapeerSessionResponse response = (UltrapeerSessionResponse) responseMessage.getBody();
		assertTrue(response.isAccepted());
	}

	@Test
	public void shouldDenySessionIfNotHealthy() throws Exception {
		Message<?> requestMessage = new AllMessage<ArrayList<UltrapeerNode>>(
				MessEngineConstants.ULTRAPEER_SESSION_REQUEST_TYPE, new ArrayList<UltrapeerNode>());
		when(ultrapeerMonitor.isHealthy()).thenReturn(false);

		stubEngine.send(requestMessage);

		Message<?> responseMessage = stubEngine.getMessage(MessEngineConstants.ULTRAPEER_SESSION_RESPONSE_TYPE);
		assertNotNull(responseMessage);
		UltrapeerSessionResponse response = (UltrapeerSessionResponse) responseMessage.getBody();
		assertFalse(response.isAccepted());
	}

	@Test
	public void shouldSetServiceProperty() throws Exception {

		String value = "value";
		String property = "property";

		service.setProperty(property, value);

		verify(ultrapeerConfig).setProperty(property, value);
	}

}
