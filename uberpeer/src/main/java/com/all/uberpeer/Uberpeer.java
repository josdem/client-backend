package com.all.uberpeer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Controller;

import com.all.backend.commons.BackendMessage;
import com.all.backend.commons.services.UberpeerService;
import com.all.dht.DhtManager;
import com.all.messengine.MessEngine;
import com.all.networking.AbstractNetworkingService;
import com.all.networking.NetworkingSocketFactory;
import com.all.services.AllService;
import com.all.shared.model.AllMessage;

@Controller("uberpeerService")
public class Uberpeer extends AbstractNetworkingService implements AllService, UberpeerService {

	private static final Log LOG = LogFactory.getLog(Uberpeer.class);

	private static final Integer UBERPEER_PORT = 10010;

	@Autowired
	private DhtManager dhtManager;
	@Autowired
	private NetworkingSocketFactory socketConfigurator;
	@Autowired
	private MessEngine messEngine;
	@Autowired
	private Properties dhtConfig;

	private IoAcceptor acceptor;
	

	@Override
	public void start() {
		if (isStarted()) {
			return;
		}
		try {
			startDht();
			startNetworkingServices();
			startUberpeerServices();
			LOG.info("UBERPEER IS UP AND RUNNING!!");
		} catch (Exception e) {
			LOG.error("COULD NOT START UBERPEER.\n", e);
		}
	}

	@Override
	public void stop() {
		stopUberpeerServices();
		stopNetworkingServices();
		stopDht();
		LOG.info("Uberpeer has stopped succesfully...");
	}
	

	@Override
	protected MessEngine getMessEngine() {
		return messEngine;
	}

	private boolean isStarted() {
		return acceptor != null && acceptor.isActive();
	}

	private void startDht() {
		LOG.info("Starting dht...");
		dhtManager.start();
	}


	private void startNetworkingServices() throws GeneralSecurityException, IOException {
		LOG.info("Starting Uberpeer in port " + UBERPEER_PORT);
		acceptor = socketConfigurator.newAcceptor();
		acceptor.setHandler(this);
		acceptor.bind(new InetSocketAddress(UBERPEER_PORT));
	}
	
	private void stopNetworkingServices() {
		if (acceptor != null && acceptor.isActive()) {
			LOG.info("Disposing Uberpeer acceptor...");
			try {
				for(IoSession managedSession: acceptor.getManagedSessions().values()){
					managedSession.close(true);
				}
			} catch (Exception e) {
				LOG.error("Could not close a session gracefully.", e);
			}
			acceptor.unbind();
			acceptor.dispose();
		}
	}

	private void stopDht() {
		if (dhtManager != null) {
			LOG.info("Stopping dht...");
			dhtManager.stop();
		}
	}


	private void startUberpeerServices() {
		messEngine.send(new AllMessage<Integer>(UberpeerConstants.START_UBERPEER_SERVICES_TYPE, UBERPEER_PORT));
	}

	private void stopUberpeerServices() {
		messEngine.send(new AllMessage<Integer>(UberpeerConstants.STOP_UBERPEER_SERVICES_TYPE, UBERPEER_PORT));		
	}


	public static void main(String[] args) {
		ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				new String[] { "/applicationContext.xml" });
		Uberpeer uberpeer = applicationContext.getBean(Uberpeer.class);
		uberpeer.start();
	}

	@Override
	public String getProperty(String property) {
		return dhtConfig.getProperty(property);
	}

	@Override
	public void setProperty(String property, String value) {
		dhtConfig.setProperty(property, value);
	}

	@Override
	public void queue(BackendMessage message) {
		messEngine.send(message.getMessage());
	}
}
