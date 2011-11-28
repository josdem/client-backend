package com.all.ultrapeer.services;

import static com.all.networking.NetworkingConstants.NETWORKING_SESSION_ID;
import static com.all.shared.messages.MessEngineConstants.FRIENDSHIP_REQUEST_RESULT_TYPE;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.all.messengine.MessEngine;
import com.all.messengine.MessageMethod;
import com.all.shared.messages.MessEngineConstants;
import com.all.shared.model.AllMessage;
import com.all.shared.model.ContactRequest;
import com.all.ultrapeer.util.AllServerProxy;

@Service
@Deprecated
// "This is handled by the client" Here for backwards compatibility.
public class FriendshipService {

	@Autowired
	private MessEngine messEngine;
	@Autowired
	private AllServerProxy restBackend;

	private final Log log = LogFactory.getLog(this.getClass());

	@MessageMethod(MessEngineConstants.FRIENDSHIP_RESPONSE_TYPE)
	public void processFriendshipResponseMessage(ContactRequest request) {
		log.info("Processing a friendship response for request " + request.getId());
		String response = request.isAccepted() ? Boolean.toString(true) : Boolean.toString(false);
		restBackend.put("contactResponseUrl", response, request.getId());
	}

	@MessageMethod(MessEngineConstants.FRIENDSHIP_REQUEST_TYPE)
	public void processFriendshipRequestMessage(AllMessage<ContactRequest> message) {
		ContactRequest request = message.getBody();
		log.info("Processing a friendship request from " + request.getRequester() + " to " + request.getRequested());
		String result = restBackend.getForObject("contactRequestUrl", String.class, request.getRequester().getId(), request
				.getRequested().getId());
		AllMessage<String> resultMessage = new AllMessage<String>(FRIENDSHIP_REQUEST_RESULT_TYPE, result.toString());
		resultMessage.putProperty(NETWORKING_SESSION_ID, message.getProperty(NETWORKING_SESSION_ID));
		messEngine.send(resultMessage);
	}

}
