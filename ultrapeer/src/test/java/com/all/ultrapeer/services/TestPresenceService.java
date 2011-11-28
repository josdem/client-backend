package com.all.ultrapeer.services;

import static com.all.networking.NetworkingConstants.NETWORKING_SESSION_ID;
import static com.all.ultrapeer.messages.UltrapeerMessages.Types.START_ULTRAPEER_SERVICES_TYPE;
import static com.all.ultrapeer.messages.UltrapeerMessages.Types.STOP_ULTRAPEER_SERVICES_TYPE;
import static com.all.ultrapeer.messages.UltrapeerMessages.Types.USER_PRESENCE_EXPIRED_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.all.dht.DhtManager;
import com.all.messengine.Message;
import com.all.messengine.impl.StubMessEngine;
import com.all.peer.commons.messages.ForwardMessage;
import com.all.peer.commons.util.PeerUtils;
import com.all.shared.messages.MessEngineConstants;
import com.all.shared.model.AllMessage;
import com.all.shared.model.ContactInfo;
import com.all.shared.model.PresenceInfo;
import com.all.ultrapeer.util.AllServerProxy;

public class TestPresenceService {

	private static final String GLOBAL_PRESENCE_KEY = "GLOBAL_PRESENCE_KEY";
	@InjectMocks
	private PresenceService presenceService = new PresenceService();
	@Spy
	private StubMessEngine stubEngine = new StubMessEngine();
	@Mock
	private DhtManager dhtManager;
	@Mock
	private ScheduledExecutorService globalPresenceExecutor;
	@Captor
	private ArgumentCaptor<Runnable> runnableCaptor;
	@Mock
	private AllServerProxy userBackend;

	private String email = "somebody@all.com";
	private PresenceInfo presenceInfo = new PresenceInfo();

	@Before
	public void shouldInitialize() throws Exception {
		MockitoAnnotations.initMocks(this);
		stubEngine.setup(presenceService);
		Set<String> registeredTypes = stubEngine.getRegisteredTypes();
		assertTrue(registeredTypes.contains(MessEngineConstants.USER_STATUS_ADV_TYPE));
		assertTrue(registeredTypes.contains(MessEngineConstants.CONTACT_STATUS_REQUEST_TYPE));
		assertTrue(registeredTypes.contains(USER_PRESENCE_EXPIRED_TYPE));
	}

	@Test
	public void shouldProcessUserOnlineStatusAdvMessageAndPushItsPresenceToItsContacts() throws Exception {
		PresenceInfo presenceInfo = new PresenceInfo();
		String email = "somebody@all.com";
		presenceInfo.setEmail(email);
		presenceInfo.setOnline(true);
		AllMessage<PresenceInfo> advMessage = new AllMessage<PresenceInfo>(MessEngineConstants.USER_STATUS_ADV_TYPE,
				presenceInfo);
		// push the presence info to the contacts
		advMessage.putProperty(MessEngineConstants.PUSH_TO, "a@all.com,b@all.com");

		stubEngine.send(advMessage);

		verify(dhtManager).put(anyString(), eq(presenceInfo));
		List<Message<?>> pushPresenceMessages = stubEngine.getSentMessages();
		pushPresenceMessages.remove(advMessage);
		assertEquals(2, pushPresenceMessages.size());
		for (Message<?> message : pushPresenceMessages) {
			assertTrue(message instanceof ForwardMessage);
			ForwardMessage fwdMessage = (ForwardMessage) message;
			assertTrue(fwdMessage.getContactId().equals("a@all.com") || fwdMessage.getContactId().equals("b@all.com"));
			assertTrue(message.getBody() instanceof AllMessage);
		}
	}

	@Test
	public void shouldProcessUserOnlineStatusAdvMessageAndPushItsPresenceToOneContact() throws Exception {
		PresenceInfo presenceInfo = new PresenceInfo();
		String email = "somebody@all.com";
		presenceInfo.setEmail(email);
		presenceInfo.setOnline(true);
		AllMessage<PresenceInfo> advMessage = new AllMessage<PresenceInfo>(MessEngineConstants.USER_STATUS_ADV_TYPE,
				presenceInfo);
		// push the presence info to the contacts
		advMessage.putProperty(MessEngineConstants.PUSH_TO, "a@all.com");

		stubEngine.send(advMessage);

		verify(dhtManager).put(anyString(), eq(presenceInfo));
		List<Message<?>> pushPresenceMessages = stubEngine.getSentMessages();
		pushPresenceMessages.remove(advMessage);
		assertEquals(1, pushPresenceMessages.size());
		for (Message<?> message : pushPresenceMessages) {
			assertTrue(message instanceof ForwardMessage);
			ForwardMessage fwdMessage = (ForwardMessage) message;
			assertEquals("a@all.com", fwdMessage.getContactId());
			assertTrue(message.getBody() instanceof AllMessage);
		}
	}

	@Test
	public void shouldNotRepublishTwiceOnlineStatusInDht() throws Exception {
		PresenceInfo presenceInfo = new PresenceInfo();
		String email = "somebody@all.com";
		presenceInfo.setEmail(email);
		presenceInfo.setOnline(true);
		AllMessage<PresenceInfo> advMessage = new AllMessage<PresenceInfo>(MessEngineConstants.USER_STATUS_ADV_TYPE,
				presenceInfo);
		String primaryKey = PeerUtils.createPresenceKey(email);
		when(dhtManager.hasLocalValue(primaryKey)).thenReturn(false, true);
		stubEngine.send(advMessage);
		stubEngine.send(advMessage);

		// verify dht put is called only once after both invocations
		verify(dhtManager).put(anyString(), eq(presenceInfo));
	}

	@Test
	public void shouldProcessUserOfflienStatusAdvMessage() throws Exception {
		presenceInfo.setEmail(email);
		presenceInfo.setOnline(false);
		String primaryKey = PeerUtils.createPresenceKey(email);
		when(dhtManager.hasLocalValue(primaryKey)).thenReturn(true);

		AllMessage<PresenceInfo> advMessage = new AllMessage<PresenceInfo>(MessEngineConstants.USER_STATUS_ADV_TYPE,
				presenceInfo);
		stubEngine.send(advMessage);

		verify(dhtManager).removeLocal(anyString());
	}

	@Test
	public void shouldProcessContactStatusRequestMessage() throws Exception {
		AllMessage<String> requestMessage = new AllMessage<String>(MessEngineConstants.CONTACT_STATUS_REQUEST_TYPE, email);
		String sessionId = "sessionId";
		requestMessage.putProperty(NETWORKING_SESSION_ID, sessionId);
		Set<PresenceInfo> presenceInfoSet = new HashSet<PresenceInfo>();
		presenceInfoSet.add(presenceInfo);
		when(dhtManager.get(anyString(), eq(PresenceInfo.class))).thenReturn(presenceInfoSet);

		stubEngine.send(requestMessage);

		verify(dhtManager).get(anyString(), eq(PresenceInfo.class));
		Message<?> responseMessage = stubEngine.getMessage(MessEngineConstants.CONTACT_STATUS_RESPONSE_TYPE);
		assertNotNull(responseMessage);
		PresenceInfo responseInfo = (PresenceInfo) responseMessage.getBody();
		assertNotNull(responseInfo);
		assertTrue(responseInfo.isOnline());
		assertEquals(sessionId, responseMessage.getProperty(NETWORKING_SESSION_ID));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldProcessContactListStatusRequestMessage() throws Exception {
		AllMessage<List<String>> requestMessage = new AllMessage<List<String>>(
				MessEngineConstants.CONTACT_LIST_STATUS_REQUEST_TYPE, Arrays.asList(email));
		String sessionId = "sessionId";
		requestMessage.putProperty(NETWORKING_SESSION_ID, sessionId);
		Set<PresenceInfo> presenceInfoSet = new HashSet<PresenceInfo>();
		presenceInfoSet.add(presenceInfo);
		when(dhtManager.get(anyString(), eq(PresenceInfo.class))).thenReturn(presenceInfoSet);

		stubEngine.send(requestMessage);

		verify(dhtManager).get(anyString(), eq(PresenceInfo.class));
		Message<?> responseMessage = stubEngine.getMessage(MessEngineConstants.CONTACT_LIST_STATUS_RESPONSE_TYPE);
		assertNotNull(responseMessage);
		List<PresenceInfo> responseList = (List<PresenceInfo>) responseMessage.getBody();
		assertNotNull(responseList);
		PresenceInfo presenceInfo = responseList.get(0);
		assertTrue(presenceInfo.isOnline());
		assertEquals(sessionId, responseMessage.getProperty(NETWORKING_SESSION_ID));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnOfflineStatusIfPresenceInfoNotFound() throws Exception {
		AllMessage<String> requestMessage = new AllMessage<String>(MessEngineConstants.CONTACT_STATUS_REQUEST_TYPE, email);
		String sessionId = "sessionId";
		requestMessage.putProperty(NETWORKING_SESSION_ID, sessionId);
		when(dhtManager.get(anyString(), eq(PresenceInfo.class))).thenReturn(Collections.EMPTY_SET);

		stubEngine.send(requestMessage);

		verify(dhtManager).get(anyString(), eq(PresenceInfo.class));
		Message<?> responseMessage = stubEngine.getMessage(MessEngineConstants.CONTACT_STATUS_RESPONSE_TYPE);
		assertNotNull(responseMessage);
		PresenceInfo presenceInfo = (PresenceInfo) responseMessage.getBody();
		assertNotNull(presenceInfo);
		assertEquals(email, presenceInfo.getEmail());
		assertFalse(presenceInfo.isOnline());
		assertEquals(sessionId, responseMessage.getProperty(NETWORKING_SESSION_ID));
	}

	@Test
	public void shouldProcessUserExpirationMessage() throws Exception {
		String user = "user@all.com";
		String presenceKey = PeerUtils.createPresenceKey(user);
		String primaryKey = PeerUtils.createPresenceKey(user);
		when(dhtManager.hasLocalValue(primaryKey)).thenReturn(true);

		stubEngine.send(new AllMessage<String>(USER_PRESENCE_EXPIRED_TYPE, user));

		verify(dhtManager).removeLocal(presenceKey);
	}

	@Test
	public void shouldLoadPreviousLocalOnlineUsersAndScheduleGlobalPresenceTasksWhenServiceStarted() throws Exception {
		when(dhtManager.getLocalValue(anyString(), eq(ArrayList.class))).thenReturn(
				new ArrayList<String>(Arrays.asList(new String[] { "user@all.com" })));
		stubEngine.send(new AllMessage<String>(START_ULTRAPEER_SERVICES_TYPE, ""));

		verify(dhtManager).getLocalValue(anyString(), eq(ArrayList.class));
		verify(globalPresenceExecutor).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(),
				eq(TimeUnit.MINUTES));
	}

	@Test
	public void shouldUpdateGlobalOnlineUsersListWithLocalOnlineUsersWhenTheseChange() throws Exception {
		startService();
		Runnable updateTask = runnableCaptor.getValue();

		updateTask.run();
		verify(dhtManager, never()).put(anyString(), anyObject());

		loginAUser();

		updateTask.run();
		verify(dhtManager).put(eq(GLOBAL_PRESENCE_KEY), anyObject());
		reset(dhtManager);

		updateTask.run();

		verify(dhtManager, never()).put(anyString(), anyObject());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldGetGlobalOnlineUsersList() throws Exception {
		startService();

		Set<ArrayList> value = new HashSet<ArrayList>();
		String userA = "userA@all.com";
		String userB = "userB@all.com";
		value.add(new ArrayList<String>(Arrays.asList(new String[] { userA })));
		value.add(new ArrayList<String>(Arrays.asList(new String[] { userB })));
		when(dhtManager.get(GLOBAL_PRESENCE_KEY, ArrayList.class)).thenReturn(value);
		ArrayList<ContactInfo> expectedContacts = new ArrayList<ContactInfo>();
		when(userBackend.postForCollection(anyString(), anyObject(), isA(Class.class), eq(ContactInfo.class))).thenReturn(
				expectedContacts);
		stubEngine.send(new AllMessage<String>(MessEngineConstants.ONLINE_USERS_LIST_REQUEST_TYPE, ""));
		Message<List<ContactInfo>> response = (Message<List<ContactInfo>>) stubEngine
				.getMessage(MessEngineConstants.ONLINE_USERS_LIST_RESPONSE_TYPE);
		List<ContactInfo> onlineUsers = response.getBody();
		assertEquals(expectedContacts, onlineUsers);

	}

	@Test
	public void shouldStopService() throws Exception {
		stubEngine.send(new AllMessage<Void>(STOP_ULTRAPEER_SERVICES_TYPE, null));
		verify(globalPresenceExecutor, never()).shutdownNow();

		startService();

		stubEngine.send(new AllMessage<Void>(STOP_ULTRAPEER_SERVICES_TYPE, null));
		verify(globalPresenceExecutor).shutdownNow();
	}

	@Test
	public void shouldNotStartTwice() throws Exception {
		AllMessage<String> startMessage = new AllMessage<String>(START_ULTRAPEER_SERVICES_TYPE, "");
		stubEngine.send(startMessage);
		stubEngine.send(startMessage);
		stubEngine.send(startMessage);
		stubEngine.send(startMessage);

		verify(globalPresenceExecutor).scheduleWithFixedDelay(runnableCaptor.capture(), anyLong(), anyLong(),
				eq(TimeUnit.MINUTES));
	}

	private void loginAUser() {
		PresenceInfo presenceInfo = new PresenceInfo();
		String email = "somebody@all.com";
		presenceInfo.setEmail(email);
		presenceInfo.setOnline(true);
		AllMessage<PresenceInfo> advMessage = new AllMessage<PresenceInfo>(MessEngineConstants.USER_STATUS_ADV_TYPE,
				presenceInfo);
		stubEngine.send(advMessage);
		verify(dhtManager).put(anyString(), anyObject());
		reset(dhtManager);

	}

	private void startService() {
		stubEngine.send(new AllMessage<String>(START_ULTRAPEER_SERVICES_TYPE, ""));

		verify(globalPresenceExecutor).scheduleWithFixedDelay(runnableCaptor.capture(), anyLong(), anyLong(),
				eq(TimeUnit.MINUTES));
	}

}
