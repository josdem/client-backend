package com.all.ultrapeer.services;

import static com.all.ultrapeer.messages.UltrapeerMessages.Types.START_ULTRAPEER_SERVICES_TYPE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.all.messengine.MessEngine;
import com.all.messengine.MessageMethod;
import com.all.networking.NetworkingConstants;
import com.all.shared.messages.MessEngineConstants;
import com.all.shared.model.AllMessage;
import com.all.shared.model.ContactInfo;
import com.all.ultrapeer.util.AllServerProxy;

@Service
@Deprecated
public class DefaultContactsService {

	private final Log log = LogFactory.getLog(this.getClass());
	private List<ContactInfo> defaultContacts = Collections.unmodifiableList(new ArrayList<ContactInfo>());
	@Autowired
	private AllServerProxy userBackend;
	@Autowired
	private MessEngine messEngine;

	@SuppressWarnings("unchecked")
	@MessageMethod(START_ULTRAPEER_SERVICES_TYPE)
	public void start() {
		log.info("Updating default contacts list...");
		List<ContactInfo> contacts = userBackend.getForCollection("defaultContactsUrl", ArrayList.class, ContactInfo.class);
		defaultContacts = Collections.unmodifiableList(contacts);
		log.info("DEFAULT_CONTACTS " + defaultContacts);
	}

	@MessageMethod(MessEngineConstants.DEFAULT_CONTACTS_REQUEST_TYPE)
	public void processDefaultContactsRequest(AllMessage<String> requestMessage) {
		log.info("Processing a default contacts request from " + requestMessage.getBody());
		AllMessage<List<ContactInfo>> responseMessage = new AllMessage<List<ContactInfo>>(
				MessEngineConstants.DEFAULT_CONTACTS_RESPONSE_TYPE, new ArrayList<ContactInfo>(defaultContacts));
		responseMessage.putProperty(NetworkingConstants.NETWORKING_SESSION_ID, requestMessage
				.getProperty(NetworkingConstants.NETWORKING_SESSION_ID));
		messEngine.send(responseMessage);
	}

	public String getInfo() {
		return "\nDefaultContacts: " + defaultContacts;
	}

}
