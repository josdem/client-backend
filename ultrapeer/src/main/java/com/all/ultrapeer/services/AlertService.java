package com.all.ultrapeer.services;

import static com.all.shared.messages.MessEngineConstants.ALERTS_REQUEST_TYPE;
import static com.all.shared.messages.MessEngineConstants.ALERTS_RESPONSE_TYPE;
import static com.all.shared.messages.MessEngineConstants.DELETE_ALERT_TYPE;
import static com.all.shared.messages.MessEngineConstants.PUT_ALERT_TYPE;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.all.messengine.MessEngine;
import com.all.messengine.Message;
import com.all.messengine.MessageMethod;
import com.all.networking.NetworkingConstants;
import com.all.shared.alert.Alert;
import com.all.shared.model.AllMessage;
import com.all.ultrapeer.util.AllServerProxy;

@Service
@Deprecated // Now  clients handle this  with the all-server directly.
public class AlertService {

	public static final String RETRIEVE_ALERTS = "retrieveAlerts";

	public static final String DELETE_ALERT = "deleteAlert";

	public static final String SAVE_ALERT = "saveAlert";

	public static final String EMAIL_ALERT = "emailAlert";

	private Log log = LogFactory.getLog(this.getClass());

	@Autowired
	private MessEngine messEngine;

	@Autowired
	private AllServerProxy allBackend;

	@MessageMethod(ALERTS_REQUEST_TYPE)
	@Deprecated
	public void requestUserAlerts(AllMessage<String> message) {
		String userId = message.getBody();

		log.info("Processing alerts request from " + userId);

		@SuppressWarnings("unchecked")
		List<Alert> currentAlerts = allBackend.getForCollection(RETRIEVE_ALERTS, ArrayList.class, Alert.class, userId);
		Message<List<Alert>> responseMessage = new AllMessage<List<Alert>>(ALERTS_RESPONSE_TYPE, currentAlerts);
		responseMessage.putProperty(NetworkingConstants.NETWORKING_SESSION_ID, message
				.getProperty(NetworkingConstants.NETWORKING_SESSION_ID));
		messEngine.send(responseMessage);
	}

	@MessageMethod(DELETE_ALERT_TYPE)
	@Deprecated
	public void delete(Alert alert) {
		allBackend.delete(DELETE_ALERT, alert.getId());
		log.info("Alert " + alert.getId() + " was removed.");
	}

	@MessageMethod(PUT_ALERT_TYPE)
	@Deprecated
	public void put(Alert alert) {
		allBackend.put(SAVE_ALERT, alert, alert.getId());

	}



}
