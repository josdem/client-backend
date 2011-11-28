package com.all.ultrapeer.services;

import static com.all.networking.NetworkingConstants.NETWORKING_SESSION_ID;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.all.messengine.MessEngine;
import com.all.messengine.MessageMethod;
import com.all.shared.messages.MessEngineConstants;
import com.all.shared.model.AllMessage;
import com.all.shared.model.ContactInfo;
import com.all.ultrapeer.util.AllServerProxy;

@Service
@Deprecated
public class SearchService {

	@Autowired
	private MessEngine messEngine;

	@Autowired
	private AllServerProxy restBackend;

	private final Log log = LogFactory.getLog(this.getClass());

	@MessageMethod(MessEngineConstants.SEARCH_CONTACTS_REQUEST_TYPE)
	public void processSearchRequestMessage(AllMessage<String> message) {
		log.info("Processing a search request message.");
		@SuppressWarnings("unchecked")
		List<ContactInfo> result = restBackend.getForCollection("searchContactsUrl", ArrayList.class, ContactInfo.class,
				message.getBody());
		AllMessage<List<ContactInfo>> responseMessage = new AllMessage<List<ContactInfo>>(
				MessEngineConstants.SEARCH_CONTACTS_RESPONSE_TYPE, result);
		responseMessage.putProperty(NETWORKING_SESSION_ID, message.getProperty(NETWORKING_SESSION_ID));
		messEngine.send(responseMessage);
	}

}
