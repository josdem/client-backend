package com.all.ultrapeer.services;

import static com.all.networking.NetworkingConstants.NETWORKING_SESSION_ID;
import static com.all.shared.messages.MessEngineConstants.CONTACT_LIST_STATUS_REQUEST_TYPE;
import static com.all.shared.messages.MessEngineConstants.CONTACT_LIST_STATUS_RESPONSE_TYPE;
import static com.all.shared.messages.MessEngineConstants.CONTACT_STATUS_REQUEST_TYPE;
import static com.all.shared.messages.MessEngineConstants.CONTACT_STATUS_RESPONSE_TYPE;
import static com.all.shared.messages.MessEngineConstants.ONLINE_USERS_LIST_REQUEST_TYPE;
import static com.all.shared.messages.MessEngineConstants.ONLINE_USERS_LIST_RESPONSE_TYPE;
import static com.all.shared.messages.MessEngineConstants.USER_STATUS_ADV_TYPE;
import static com.all.ultrapeer.messages.UltrapeerMessages.Types.START_ULTRAPEER_SERVICES_TYPE;
import static com.all.ultrapeer.messages.UltrapeerMessages.Types.STOP_ULTRAPEER_SERVICES_TYPE;
import static com.all.ultrapeer.messages.UltrapeerMessages.Types.USER_PRESENCE_EXPIRED_TYPE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.all.commons.IncrementalNamedThreadFactory;
import com.all.dht.DhtManager;
import com.all.messengine.MessEngine;
import com.all.messengine.MessageMethod;
import com.all.peer.commons.messages.ForwardMessage;
import com.all.peer.commons.util.PeerUtils;
import com.all.shared.messages.MessEngineConstants;
import com.all.shared.model.AllMessage;
import com.all.shared.model.ContactInfo;
import com.all.shared.model.PresenceInfo;
import com.all.ultrapeer.util.AllServerProxy;

@Service
public class PresenceService {

	private static final String GLOBAL_PRESENCE_KEY = "GLOBAL_PRESENCE_KEY";

	private static final long UPDATE_GLOBAL_PRESENCE_DELAY = 2;

	private final Log log = LogFactory.getLog(this.getClass());

	private ScheduledExecutorService globalPresenceExecutor = Executors
			.newSingleThreadScheduledExecutor(new IncrementalNamedThreadFactory("GlobalPresenceThread"));

	private final Set<String> localOnlineUsers = Collections.synchronizedSet(new HashSet<String>());
	@Autowired
	private MessEngine messEngine;
	@Autowired
	private DhtManager dhtManager;
	@Autowired
	private AllServerProxy userBackend;

	private AtomicBoolean updateGlobalOnlineUsers = new AtomicBoolean(false);

	private AtomicBoolean started = new AtomicBoolean(false);

	@MessageMethod(START_ULTRAPEER_SERVICES_TYPE)
	public synchronized void start() {
		if (started.get()) {
			throw new IllegalStateException("PresenceService was already started.");
		}
		loadLocalOnlineUsers();
		globalPresenceExecutor.scheduleWithFixedDelay(new UpdateGlobalOnlineUsersTask(), 0, UPDATE_GLOBAL_PRESENCE_DELAY,
				TimeUnit.MINUTES);
		started.set(true);
		log.info("PresenceService succesfully started.");
	}

	@PreDestroy
	@MessageMethod(STOP_ULTRAPEER_SERVICES_TYPE)
	public synchronized void stop() {
		if (!started.get()) {
			return;
		}
		updateGlobalOnlineUsers();
		globalPresenceExecutor.shutdownNow();
		localOnlineUsers.clear();
		globalPresenceExecutor = Executors.newSingleThreadScheduledExecutor(new IncrementalNamedThreadFactory(
				"GlobalPresenceThread"));
		started.set(false);
		log.info("PresenceService succesfully stopped.");
	}

	@MessageMethod(USER_STATUS_ADV_TYPE)
	public void processUserStatusAdvertiseMessage(AllMessage<PresenceInfo> message) {
		PresenceInfo presenceInfo = message.getBody();
		boolean isOnline = presenceInfo.isOnline();
		String email = presenceInfo.getEmail();
		String presenceKey = PeerUtils.createPresenceKey(email);
		// Avoid republishing values on the DHT.
		if (isOnline && !dhtManager.hasLocalValue(presenceKey)) {
			if (localOnlineUsers.add(email)) {
				log.info(email + " has logged in.");
				updateGlobalOnlineUsers.set(true);
			}
			dhtManager.put(presenceKey, presenceInfo);
			pushPresenceToContacts(message, presenceInfo);
		} else if (!isOnline && dhtManager.hasLocalValue(presenceKey)) {
			dhtManager.removeLocal(presenceKey);
			pushPresenceToContacts(message, presenceInfo);
			if (localOnlineUsers.remove(email)) {
				log.info(email + " has logged out.");
				updateGlobalOnlineUsers.set(true);
			}
		}
		log.info("OnLine Users: " + localOnlineUsers.size());
	}

	@MessageMethod(USER_PRESENCE_EXPIRED_TYPE)
	public void processUserPresenceExpirationMessage(String userId) {
		String presenceKey = PeerUtils.createPresenceKey(userId);
		if (dhtManager.hasLocalValue(presenceKey)) {
			log.info("Expiring presence of user " + userId);
			dhtManager.removeLocal(presenceKey);
			if (localOnlineUsers.remove(userId)) {
				log.info(userId + " session has expired. Updating status to offline.");
				updateGlobalOnlineUsers.set(true);
			}
		}
		log.info("OnLine Users: " + localOnlineUsers.size());
	}

	private void pushPresenceToContacts(AllMessage<PresenceInfo> message, PresenceInfo presenceInfo) {
		String pushTo = message.getProperty(MessEngineConstants.PUSH_TO);
		if (pushTo != null && !pushTo.isEmpty()) {
			for (String contact : getContactsList(pushTo)) {
				if (!contact.isEmpty()) {
					AllMessage<PresenceInfo> pushMessage = new AllMessage<PresenceInfo>(
							MessEngineConstants.CONTACT_STATUS_PUSH_TYPE, presenceInfo);
					messEngine.send(new ForwardMessage(pushMessage, contact));
				}
			}
		}
	}

	private String[] getContactsList(String pushTo) {
		String[] contacts = null;
		if (pushTo != null) {
			if (pushTo.contains(",")) {
				contacts = pushTo.split(",");
			} else {
				contacts = new String[] { pushTo };
			}
		}
		return contacts;
	}

	@MessageMethod(CONTACT_STATUS_REQUEST_TYPE)
	public void processContactStatusRequestMessage(AllMessage<String> message) {
		String contactId = message.getBody();
		log.info("Processing a contact status request for " + contactId);
		PresenceInfo presenceInfo = getPresenceInfo(contactId);
		AllMessage<PresenceInfo> responseMessage = new AllMessage<PresenceInfo>(CONTACT_STATUS_RESPONSE_TYPE, presenceInfo);
		responseMessage.putProperty(NETWORKING_SESSION_ID, message.getProperty(NETWORKING_SESSION_ID));
		messEngine.send(responseMessage);
	}

	private PresenceInfo getPresenceInfo(String contactId) {
		Set<PresenceInfo> values = dhtManager.get(PeerUtils.createPresenceKey(contactId), PresenceInfo.class);
		PresenceInfo presenceInfo = new PresenceInfo();
		presenceInfo.setEmail(contactId);
		presenceInfo.setOnline(!values.isEmpty());
		return presenceInfo;
	}

	@MessageMethod(CONTACT_LIST_STATUS_REQUEST_TYPE)
	public void processContactListStatusRequestMessage(AllMessage<List<String>> message) {
		List<String> contactList = message.getBody();
		log.info("Processing a status request for contacts " + contactList);
		List<PresenceInfo> responseList = new ArrayList<PresenceInfo>();
		for (String contactId : contactList) {
			responseList.add(getPresenceInfo(contactId));
		}
		AllMessage<List<PresenceInfo>> responseMessage = new AllMessage<List<PresenceInfo>>(
				CONTACT_LIST_STATUS_RESPONSE_TYPE, responseList);
		responseMessage.putProperty(NETWORKING_SESSION_ID, message.getProperty(NETWORKING_SESSION_ID));
		messEngine.send(responseMessage);
	}

	@MessageMethod(ONLINE_USERS_LIST_REQUEST_TYPE)
	public void processOnlineUsersListRequest(AllMessage<String> message) {
		log.info("Processing a global online users list request.");
		AllMessage<List<ContactInfo>> responseMessage = new AllMessage<List<ContactInfo>>(ONLINE_USERS_LIST_RESPONSE_TYPE,
				getGlobalOnlineUsers());
		responseMessage.putProperty(NETWORKING_SESSION_ID, message.getProperty(NETWORKING_SESSION_ID));
		messEngine.send(responseMessage);
	}

	@SuppressWarnings("unchecked")
	private List<ContactInfo> getGlobalOnlineUsers() {
		Set<ArrayList> allLocalOnlineUsers = dhtManager.get(GLOBAL_PRESENCE_KEY, ArrayList.class);
		// here we have to call backend
		List<String> globalOnlineUsers = new ArrayList<String>();
		for (List list : allLocalOnlineUsers) {
			globalOnlineUsers.addAll(list);
		}
		ArrayList responseUsers = userBackend.postForCollection("allOnlineUsersUrl", globalOnlineUsers, ArrayList.class,
				ContactInfo.class);
		return responseUsers;
	}

	private void updateGlobalOnlineUsers() {
		if (updateGlobalOnlineUsers.getAndSet(false)) {
			log.info("Updating global online user list.");
			Set<String> currentLocalOnlineUsers = new HashSet<String>();
			synchronized (localOnlineUsers) {
				currentLocalOnlineUsers.addAll(localOnlineUsers);
			}
			dhtManager.put(GLOBAL_PRESENCE_KEY, currentLocalOnlineUsers);
		}
	}

	@SuppressWarnings("unchecked")
	private void loadLocalOnlineUsers() {
		List<String> previousLocalOnline = dhtManager.getLocalValue(GLOBAL_PRESENCE_KEY, ArrayList.class);
		if (previousLocalOnline != null) {
			localOnlineUsers.addAll(previousLocalOnline);
		}
	}

	private final class UpdateGlobalOnlineUsersTask implements Runnable {
		@Override
		public void run() {
			try {
				updateGlobalOnlineUsers();
			} catch (Exception e) {
				log.error("Unexpected error updating global online users list.", e);
			}
		}
	}

	public String getInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("\nOnline Users: " + localOnlineUsers.size());
		sb.append("\n");
		sb.append(localOnlineUsers.toString());
		return sb.toString();
	}
}
