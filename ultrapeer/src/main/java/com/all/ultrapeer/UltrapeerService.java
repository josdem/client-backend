package com.all.ultrapeer;

import static com.all.networking.NetworkingConstants.NETWORKING_SESSION_ID;
import static com.all.shared.messages.MessEngineConstants.ALERTS_RESPONSE_TYPE;
import static com.all.shared.messages.MessEngineConstants.AVATAR_RESPONSE_TYPE;
import static com.all.shared.messages.MessEngineConstants.CHAT_MESSAGE_RESPONSE;
import static com.all.shared.messages.MessEngineConstants.CONTACT_AVATAR_PUSH_TYPE;
import static com.all.shared.messages.MessEngineConstants.CONTACT_LIST_RESPONSE_TYPE;
import static com.all.shared.messages.MessEngineConstants.CONTACT_LIST_STATUS_RESPONSE_TYPE;
import static com.all.shared.messages.MessEngineConstants.CONTACT_PROFILE_PUSH_TYPE;
import static com.all.shared.messages.MessEngineConstants.CONTACT_QUOTE_PUSH_TYPE;
import static com.all.shared.messages.MessEngineConstants.CONTACT_STATUS_PUSH_TYPE;
import static com.all.shared.messages.MessEngineConstants.CONTACT_STATUS_RESPONSE_TYPE;
import static com.all.dht.DhtConstants.CURRENT_ULTRAPEERS_SET_REQUEST_TYPE;
import static com.all.dht.DhtConstants.CURRENT_ULTRAPEERS_SET_RESPONSE_TYPE;
import static com.all.shared.messages.MessEngineConstants.DEFAULT_CONTACTS_RESPONSE_TYPE;
import static com.all.shared.messages.MessEngineConstants.FRIENDSHIP_REQUEST_RESULT_TYPE;
import static com.all.shared.messages.MessEngineConstants.IMPORT_CONTACTS_RESPONSE_TYPE;
import static com.all.shared.messages.MessEngineConstants.LOGIN_PASSWORD_RESPONSE_TYPE;
import static com.all.shared.messages.MessEngineConstants.LOGIN_RESPONSE_TYPE;
import static com.all.shared.messages.MessEngineConstants.LOGIN_SIGNUP_RESPONSE_TYPE;
import static com.all.shared.messages.MessEngineConstants.ONLINE_USERS_LIST_RESPONSE_TYPE;
import static com.all.shared.messages.MessEngineConstants.PUSH_ALERT_TYPE;
import static com.all.shared.messages.MessEngineConstants.PUSH_PENDING_EMAIL_TYPE;
import static com.all.shared.messages.MessEngineConstants.PUSH_REST_UPLOAD_TRACK_REQUEST_TYPE;
import static com.all.shared.messages.MessEngineConstants.SEARCH_CONTACTS_RESPONSE_TYPE;
import static com.all.shared.messages.MessEngineConstants.SEEDER_TRACK_LIST_RESPONSE_TYPE;
import static com.all.shared.messages.MessEngineConstants.SYNC_LIBRARY_MERGE_RESPONSE;
import static com.all.shared.messages.MessEngineConstants.TRACK_SEEDERS_RESPONSE_TYPE;
import static com.all.shared.messages.MessEngineConstants.ULTRAPEER_SESSION_REQUEST_TYPE;
import static com.all.shared.messages.MessEngineConstants.ULTRAPEER_SESSION_RESPONSE_TYPE;
import static com.all.shared.messages.MessEngineConstants.UPDATE_CONTACT_PROFILE_RESPONSE;
import static com.all.ultrapeer.messages.UltrapeerMessages.Properties.MP_UNWRAP_BEFORE_SEND;
import static com.all.ultrapeer.messages.UltrapeerMessages.Types.START_ULTRAPEER_SERVICES_TYPE;
import static com.all.ultrapeer.messages.UltrapeerMessages.Types.STOP_ULTRAPEER_SERVICES_TYPE;
import static com.all.ultrapeer.messages.UltrapeerMessages.Types.USER_PRESENCE_EXPIRED_TYPE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import com.all.commons.IncrementalNamedThreadFactory;
import com.all.dht.DhtManager;
import com.all.messengine.MessEngine;
import com.all.messengine.MessageListener;
import com.all.messengine.MessageMethod;
import com.all.networking.AbstractNetworkingService;
import com.all.networking.NetworkingConstants;
import com.all.networking.NetworkingMessage;
import com.all.networking.NetworkingSocketFactory;
import com.all.services.AllService;
import com.all.services.ServiceInvoker;
import com.all.shared.AllConstants;
import com.all.shared.messages.MessEngineConstants;
import com.all.shared.model.AllMessage;
import com.all.shared.model.UltrapeerNode;
import com.all.shared.model.UltrapeerSessionResponse;

@Service
public class UltrapeerService extends AbstractNetworkingService implements AllService, MessageListener<AllMessage<?>> {

	private static final Log LOG = LogFactory.getLog(UltrapeerService.class);

	private static final String DISCOVERY_DELAY_KEY = "ultrapeer_discovery_delay";

	private static final String[] REGISTERED_TYPES = new String[] { CONTACT_STATUS_RESPONSE_TYPE, ALERTS_RESPONSE_TYPE,
			FRIENDSHIP_REQUEST_RESULT_TYPE, SEARCH_CONTACTS_RESPONSE_TYPE, SYNC_LIBRARY_MERGE_RESPONSE, AVATAR_RESPONSE_TYPE,
			LOGIN_RESPONSE_TYPE, IMPORT_CONTACTS_RESPONSE_TYPE, LOGIN_SIGNUP_RESPONSE_TYPE, LOGIN_PASSWORD_RESPONSE_TYPE,
			CHAT_MESSAGE_RESPONSE, CONTACT_LIST_RESPONSE_TYPE, UPDATE_CONTACT_PROFILE_RESPONSE, PUSH_ALERT_TYPE,
			CONTACT_STATUS_PUSH_TYPE, ULTRAPEER_SESSION_RESPONSE_TYPE, ONLINE_USERS_LIST_RESPONSE_TYPE,
			DEFAULT_CONTACTS_RESPONSE_TYPE, PUSH_REST_UPLOAD_TRACK_REQUEST_TYPE, TRACK_SEEDERS_RESPONSE_TYPE,
			SEEDER_TRACK_LIST_RESPONSE_TYPE, CONTACT_LIST_STATUS_RESPONSE_TYPE, PUSH_PENDING_EMAIL_TYPE,
			CONTACT_QUOTE_PUSH_TYPE, CONTACT_AVATAR_PUSH_TYPE, CONTACT_PROFILE_PUSH_TYPE };

	private final ScheduledExecutorService discoveryExecutor = Executors
			.newSingleThreadScheduledExecutor(new IncrementalNamedThreadFactory("UltrapeerDiscoveryThread"));

	private final Map<Long, IoSession> peerSessions = new HashMap<Long, IoSession>();
	private final Map<String, Long> aliasToId = new HashMap<String, Long>();
	private final Map<Long, String> idToAlias = new HashMap<Long, String>();

	@Autowired
	private MessEngine messEngine;
	@Autowired
	private DhtManager dhtManager;
	@Autowired
	private NetworkingSocketFactory socketConfigurator;
	@Autowired
	private UltrapeerConfig ultrapeerConfig;
	@Autowired
	private UltrapeerMonitor ultrapeerMonitor;

	private IoAcceptor acceptor;

	private Set<UltrapeerNode> currentUltrapeers = new HashSet<UltrapeerNode>();

	@Override
	public void start() {
		if (isStarted()) {
			return;
		}
		try {
			registerMessageListeners();
			startDht();
			startServices();
			startNetworkingService();
			LOG.info("ULTRAPEER IS UP AND RUNNING!!");
		} catch (Exception e) {
			LOG.error(e, e);
		}

	}

	@MessageMethod(ULTRAPEER_SESSION_REQUEST_TYPE)
	public void processSessionRequest(AllMessage<ArrayList<UltrapeerNode>> request) {
		ArrayList<UltrapeerNode> clientList = request.getBody();
		UltrapeerSessionResponse response = new UltrapeerSessionResponse();
		response.setAccepted(ultrapeerMonitor.isHealthy());
		response.setNewUltrapeers(getNewUltrapeers(clientList));
		response.setDeprecatedUltrapeers(getDeprecatedUltrapeers(clientList));
		AllMessage<UltrapeerSessionResponse> responseMessage = new AllMessage<UltrapeerSessionResponse>(
				ULTRAPEER_SESSION_RESPONSE_TYPE, response);
		responseMessage.putProperty(NETWORKING_SESSION_ID, request.getProperty(NETWORKING_SESSION_ID));
		messEngine.send(responseMessage);
	}

	@MessageMethod(CURRENT_ULTRAPEERS_SET_RESPONSE_TYPE)
	public void setUltrapeers(Set<UltrapeerNode> ultrapeers) {
		currentUltrapeers = ultrapeers;
	}

	private void startServices() {
		messEngine.send(new AllMessage<Void>(START_ULTRAPEER_SERVICES_TYPE, null));
	}

	@Override
	protected void sessionUpdated(IoSession session, NetworkingMessage networkingMessage) {
		// TODO validate networking session or deny session
		addAlias(networkingMessage.getSender(), session);
		try {
			String publicIp = ((InetSocketAddress) session.getRemoteAddress()).getAddress().getHostAddress();
			networkingMessage.getBody().putProperty(MessEngineConstants.MP_SOURCE_PUBLIC_IP, publicIp);
		} catch (Exception e) {
			LOG.error("Could not add public ip property to received message.", e);
		}
	}

	@Override
	public void sessionCreated(IoSession session) {
		addSession(session);
	}

	@Override
	public void sessionClosed(IoSession session) {
		removeSession(session);
	}

	private void startNetworkingService() throws GeneralSecurityException, IOException {
		LOG.info("Starting ultrapeer acceptor");
		discoveryExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				messEngine.send(new AllMessage<Serializable>(CURRENT_ULTRAPEERS_SET_REQUEST_TYPE, null));
			}
		}, 0, ultrapeerConfig.getTypedProperty(DISCOVERY_DELAY_KEY, Long.class), TimeUnit.MINUTES);
		acceptor = socketConfigurator.newAcceptor();
		acceptor.setHandler(this);
		acceptor.bind(new InetSocketAddress(AllConstants.ULTRAPEER_PORT));
	}

	private List<UltrapeerNode> getDeprecatedUltrapeers(List<UltrapeerNode> clientList) {
		clientList.removeAll(currentUltrapeers);
		return clientList.isEmpty() ? null : clientList;
	}

	private List<UltrapeerNode> getNewUltrapeers(List<UltrapeerNode> clientList) {
		List<UltrapeerNode> newUltrapeers = new ArrayList<UltrapeerNode>(currentUltrapeers);
		newUltrapeers.removeAll(clientList);
		return newUltrapeers.isEmpty() ? null : newUltrapeers;
	}

	private Long addSession(IoSession session) {
		Long id = session.getId();
		peerSessions.put(id, session);
		return id;
	}

	private void removeSession(IoSession session) {
		peerSessions.remove(session.getId());
		String alias = idToAlias.remove(session.getId());
		if (alias != null) {
			aliasToId.remove(alias);
			messEngine.send(new AllMessage<String>(USER_PRESENCE_EXPIRED_TYPE, alias));
		}
	}

	private Long addAlias(String alias, IoSession session) {
		addSession(session);
		if (alias != null) {
			aliasToId.put(alias, session.getId());
			idToAlias.put(session.getId(), alias);
		}
		return session.getId();
	}

	private void startDht() {
		LOG.info("Starting dht...");
		dhtManager.start();
	}

	private void registerMessageListeners() {
		LOG.info("starting listeners...");
		for (String messageType : REGISTERED_TYPES) {
			messEngine.addMessageListener(messageType, this);
		}
	}

	@PreDestroy
	@Override
	public void stop() {
		stopNetworkingService();
		removeMessageListeners();
		stopServices();
		stopDht();
	}

	private void stopServices() {
		messEngine.send(new AllMessage<Void>(STOP_ULTRAPEER_SERVICES_TYPE, null));
		try {
			LOG.info("Stopping Ultrapeer services...");
			Thread.sleep(1000);
		} catch (Exception e) {
		}
	}

	private void stopDht() {
		if (dhtManager != null) {
			LOG.info("Stopping DHT manager...");
			dhtManager.stop();
		}
	}

	private void stopNetworkingService() {
		if (acceptor != null && acceptor.isActive()) {
			LOG.info("Disposing ultrapeer acceptor...");
			discoveryExecutor.shutdownNow();
			try {
				for (IoSession managedSession : acceptor.getManagedSessions().values()) {
					managedSession.close(true);
				}
			} catch (Exception e) {
				LOG.error("Could not close a session gracefully.", e);
			}
			acceptor.unbind();
			acceptor.dispose();
		}
	}

	private void removeMessageListeners() {
		if (messEngine != null) {
			LOG.info("removing messEngineListeners");
			for (String messageType : REGISTERED_TYPES) {
				messEngine.removeMessageListener(messageType, this);
			}
		}
	}

	@Override
	public void onMessage(AllMessage<?> message) {
		IoSession session = getSession(message);
		if (session == null) {
			LOG.warn("Could not find the leaf session to deliver this " + message.getType() + "  message.");
			return;
		}
		boolean shouldUnwrap = message.getProperty(MP_UNWRAP_BEFORE_SEND) != null;
		write(session, ultrapeerConfig.getName(), (AllMessage<?>) (shouldUnwrap ? message.getBody() : message));
	}

	private IoSession getSession(AllMessage<?> message) {
		String forwardTo = message.getProperty(MessEngineConstants.PROP_FORWARD_TO);
		if (forwardTo != null) {
			return getSession(forwardTo);
		} else {
			Long sessionId = Long.parseLong(message.getProperty(NetworkingConstants.NETWORKING_SESSION_ID));
			return getSession(sessionId);
		}
	}

	protected IoSession getSession(Long id) {
		return peerSessions.get(id);
	}

	protected IoSession getSession(String alias) {
		Long id = aliasToId.get(alias);
		if (id == null) {
			LOG.warn("Could not find session for " + alias);
			return null;
		}
		return peerSessions.get(id);
	}

	public boolean isStarted() {
		return acceptor != null && acceptor.isActive();
	}

	@Override
	protected MessEngine getMessEngine() {
		return messEngine;
	}

	@Override
	public String getProperty(String property) {
		return ultrapeerConfig.getProperty(property);
	}

	@Override
	public void setProperty(String property, String value) {
		ultrapeerConfig.setProperty(property, value);
	}

	public static List<String> getRegisteredTypes() {
		return Arrays.asList(REGISTERED_TYPES);
	}

	public static void runInteractiveMode(ConfigurableApplicationContext applicationContext,
			UltrapeerService ultraPeerService, ServiceInvoker monitorable, BufferedReader in) throws IOException {
		while (true) {
			LOG.info(" $ ");
			String line = in.readLine();
			line = line.trim();
			if (line.equals("")) {
				continue;
			}
			try {
				if (line.indexOf("shutdown") >= 0) {
					applicationContext.close();
					break;
				} else {
					executeCommand(ultraPeerService, monitorable, line);
				}
			} catch (Exception e) {
				LOG.error(e, e);
			}
		}
	}

	private static void executeCommand(UltrapeerService ultraPeerService, ServiceInvoker invoker, String line) {
		if (line.indexOf("start") >= 0) {
			ultraPeerService.start();
		} else if (line.indexOf("stop") >= 0) {
			ultraPeerService.stop();
		} else {
			String getCommand = "invoke";
			if (line.indexOf(getCommand) >= 0) {
				String arg = line.substring(line.indexOf(getCommand) + getCommand.length()).trim();
				LOG.info(invoker.invoke(arg));
			} else {
				LOG.error("Could not execute command: " + line);
			}
		}
	}

	public static void main(String[] args) throws IOException {
		String[] configLocations = new String[] { "/applicationContext.xml" };
		ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext(configLocations);
		UltrapeerService ultraPeerService = applicationContext.getBean(UltrapeerService.class);
		ServiceInvoker monitorable = applicationContext.getBean(ServiceInvoker.class);
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		runInteractiveMode(applicationContext, ultraPeerService, monitorable, in);
		System.exit(0);
	}

}
