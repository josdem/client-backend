package com.all.ultrapeer.services;

import static com.all.networking.NetworkingConstants.NETWORKING_SESSION_ID;
import static com.all.shared.messages.MessEngineConstants.AVATAR_OWNER;
import static com.all.shared.messages.MessEngineConstants.AVATAR_REQUEST_TYPE;
import static com.all.shared.messages.MessEngineConstants.AVATAR_RESPONSE_TYPE;
import static com.all.shared.messages.MessEngineConstants.SENDER_ID;
import static com.all.shared.messages.MessEngineConstants.UPDATE_USER_AVATAR_TYPE;
import static com.all.shared.messages.MessEngineConstants.UPDATE_USER_PROFILE_TYPE;
import static com.all.shared.messages.MessEngineConstants.UPDATE_USER_QUOTE_TYPE;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.all.messengine.MessEngine;
import com.all.messengine.MessageMethod;
import com.all.shared.messages.MessEngineConstants;
import com.all.shared.model.AllMessage;
import com.all.shared.model.Avatar;
import com.all.shared.model.User;
import com.all.ultrapeer.util.AllServerProxy;
import com.all.ultrapeer.util.BackendProxy;

@Service
public class UserProfileService {

	private final Log log = LogFactory.getLog(UserProfileService.class);
	@Autowired
	private MessEngine messEngine;
	@Autowired
	private AllServerProxy userBackend;

	@MessageMethod(UPDATE_USER_PROFILE_TYPE)
	public void processUpdateProfileMessage(AllMessage<User> message) {
		log.info("Processing an update profile request.");
		User user = message.getBody();
		String[] urlVars = BackendProxy.urlVars(message.getProperty(SENDER_ID));
		String[][] headers = BackendProxy.headers(BackendProxy.header(MessEngineConstants.PUSH_TO,
				message.getProperty(MessEngineConstants.PUSH_TO)));
		userBackend.send("updateProfileUrl", HttpMethod.PUT, user, urlVars, headers);
	}

	@MessageMethod(UPDATE_USER_AVATAR_TYPE)
	public void processUpdateAvatarMessage(AllMessage<Avatar> message) {
		try {
			log.info("Processing an avatar update request");
			// user/avatar
			String[][] headers = BackendProxy.headers(BackendProxy.header(MessEngineConstants.PUSH_TO,
					message.getProperty(MessEngineConstants.PUSH_TO)));
			userBackend.send("updateAvatarUrl", HttpMethod.PUT, message.getBody(), null, headers);
		} catch (RestClientException e) {
			log.error("error in update avatar", e);
		}
	}

	@MessageMethod(UPDATE_USER_QUOTE_TYPE)
	public void processUpdateQuoteMessage(AllMessage<String> message) {
		log.info("Processing a quote update request from " + message.getProperty(SENDER_ID));
		String[] urlVars = BackendProxy.urlVars(message.getProperty(SENDER_ID));
		String[][] headers = BackendProxy.headers(BackendProxy.header(MessEngineConstants.PUSH_TO,
				message.getProperty(MessEngineConstants.PUSH_TO)));
		userBackend.send("updateQuoteUrl", HttpMethod.PUT, message.getBody(), urlVars, headers);

	}

	@MessageMethod(AVATAR_REQUEST_TYPE)
	public void processAvatarRequestMessage(AllMessage<String> message) {
		log.info("Processing an avatar request for " + message.getProperty(AVATAR_OWNER));
		Avatar avatar = userBackend.getForObject("getAvatarUrl", Avatar.class, message.getBody());
		AllMessage<Avatar> responseMessage = new AllMessage<Avatar>(AVATAR_RESPONSE_TYPE, avatar);

		responseMessage.putProperty(AVATAR_OWNER, message.getProperty(AVATAR_OWNER));
		responseMessage.putProperty(NETWORKING_SESSION_ID, message.getProperty(NETWORKING_SESSION_ID));
		log.debug("RESPONDING AVATAR REQUEST FOR " + message.getProperty(AVATAR_OWNER));
		messEngine.send(responseMessage);
	}

}
