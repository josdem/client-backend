package com.all.ultrapeer.services;

import static com.all.networking.NetworkingConstants.NETWORKING_SESSION_ID;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.limewire.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.all.messengine.MessEngine;
import com.all.messengine.MessageMethod;
import com.all.shared.messages.MessEngineConstants;
import com.all.shared.model.AllMessage;
import com.all.shared.model.ContactInfo;
import com.all.ultrapeer.util.AllServerProxy;




@Service
public class ContactListService {

	private Log log = LogFactory.getLog(this.getClass());
	@Autowired
	private MessEngine messEngine;

	@Autowired
	private AllServerProxy restBackend;

	@MessageMethod(MessEngineConstants.UPDATE_CONTACT_PROFILE_REQUEST)
	@Deprecated
	public void processUpdateContactProfileRequestMessage(AllMessage<ContactInfo> message) {
		log.info("Processing a " + message.getType() + " for Contact with id " + message.getBody().getId());
		ContactInfo contactInfo = restBackend
				.postForObject("updateContactProfileUrl", message.getBody(), ContactInfo.class);
		AllMessage<ContactInfo> responseMessage = new AllMessage<ContactInfo>(
				MessEngineConstants.UPDATE_CONTACT_PROFILE_RESPONSE, contactInfo);
		responseMessage.putProperty(NETWORKING_SESSION_ID, message.getProperty(NETWORKING_SESSION_ID));
		messEngine.send(responseMessage);
	}

	@MessageMethod(MessEngineConstants.DELETE_PENDING_EMAILS_TYPE)
	@Deprecated
	public void processDeletePendingEmailMessage(String params) {
		log.info("Processing a delete pending email request.");
		restBackend.delete("deletePendingEmailsUrl", params);
	}

	@Deprecated
	@MessageMethod(MessEngineConstants.DELETE_CONTACTS_TYPE)
	public void processDeleteContactsRequest(AllMessage<String> message) {
		log.info("Processing a delete contact request from " + message.getProperty(MessEngineConstants.SENDER_ID));
		restBackend.delete("deleteContactsUrl", message.getProperty(MessEngineConstants.SENDER_ID), message.getBody());
	}


	@Deprecated
	@SuppressWarnings("unchecked")
	@MessageMethod(MessEngineConstants.CONTACT_LIST_REQUEST_TYPE)
	public void processContactListRequest(AllMessage<String> message) {
		log.info("Processing a contact list request from " + message.getBody());
		List<ContactInfo> contactList = restBackend.getForCollection("contactListUrl", ArrayList.class, ContactInfo.class,
				message.getBody());
		AllMessage<List<ContactInfo>> responseMessage = new AllMessage<List<ContactInfo>>(
				MessEngineConstants.CONTACT_LIST_RESPONSE_TYPE, contactList);
		responseMessage.putProperty(NETWORKING_SESSION_ID, message.getProperty(NETWORKING_SESSION_ID));
		messEngine.send(responseMessage);
	}

}
