package com.all.ultrapeer.services;

import static com.all.networking.NetworkingConstants.NETWORKING_SESSION_ID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.all.messengine.MessEngine;
import com.all.messengine.MessageMethod;
import com.all.shared.messages.CrawlerRequest;
import com.all.shared.messages.CrawlerResponse;
import com.all.shared.messages.MessEngineConstants;
import com.all.shared.model.AllMessage;
import com.all.ultrapeer.util.AllServerProxy;

@Service
@Deprecated
public class CrawlerService {

	private final Log log = LogFactory.getLog(this.getClass());
	@Autowired
	private MessEngine messEngine;
	@Autowired
	private AllServerProxy userBackend;

	@MessageMethod(MessEngineConstants.IMPORT_CONTACTS_REQUEST_TYPE)
	public void processImportContactsRequest(AllMessage<CrawlerRequest> message) {
		log.info("Prcessing a crawler request...");
		CrawlerResponse responseBody = userBackend.postForObject("crawlerRequestUrl", message.getBody(),
				CrawlerResponse.class);
		AllMessage<CrawlerResponse> responseMessage = new AllMessage<CrawlerResponse>(
				MessEngineConstants.IMPORT_CONTACTS_RESPONSE_TYPE, responseBody);
		responseMessage.putProperty(NETWORKING_SESSION_ID, message.getProperty(NETWORKING_SESSION_ID));
		messEngine.send(responseMessage);
	}

}
