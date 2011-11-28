package com.all.ultrapeer.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.all.messengine.MessEngine;
import com.all.messengine.MessageMethod;
import com.all.networking.NetworkingConstants;
import com.all.shared.messages.MessEngineConstants;
import com.all.shared.model.AllMessage;
import com.all.shared.model.PendingEmail;
import com.all.ultrapeer.util.AllServerProxy;

@Service
@Deprecated
public class EmailService {

	@Autowired
	private MessEngine messEngine;
	@Autowired
	private AllServerProxy userBackend;

	@MessageMethod(MessEngineConstants.SEND_EMAIL_TYPE)
	public void processSendEmailMessage(AllMessage<PendingEmail> message) {
		PendingEmail pendingEmail = message.getBody();
		PendingEmail response = userBackend.postForObject("emailUrl", pendingEmail, PendingEmail.class);
		if (response.getId() != null) {
			AllMessage<PendingEmail> pushMessage = new AllMessage<PendingEmail>(MessEngineConstants.PUSH_PENDING_EMAIL_TYPE,
					response);
			pushMessage.putProperty(NetworkingConstants.NETWORKING_SESSION_ID, message
					.getProperty(NetworkingConstants.NETWORKING_SESSION_ID));
			messEngine.send(pushMessage);
		}
	}

}
