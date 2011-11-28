package com.all.ultrapeer.services;

import static com.all.networking.NetworkingConstants.NETWORKING_SESSION_ID;
import static com.all.shared.messages.MessEngineConstants.LOGIN_ABOUT_US_REQUEST_TYPE;
import static com.all.shared.messages.MessEngineConstants.LOGIN_PASSWORD_REQUEST_TYPE;
import static com.all.shared.messages.MessEngineConstants.LOGIN_PASSWORD_RESPONSE_TYPE;
import static com.all.shared.messages.MessEngineConstants.LOGIN_REQUEST_TYPE;
import static com.all.shared.messages.MessEngineConstants.LOGIN_RESPONSE_TYPE;
import static com.all.shared.messages.MessEngineConstants.LOGIN_SIGNUP_REQUEST_TYPE;
import static com.all.shared.messages.MessEngineConstants.LOGIN_SIGNUP_RESPONSE_TYPE;
import static com.all.shared.messages.MessEngineConstants.USAGE_STATS_TYPE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.all.messengine.MessEngine;
import com.all.messengine.MessageMethod;
import com.all.shared.command.LoginCommand;
import com.all.shared.login.LoginError;
import com.all.shared.messages.LoginResponse;
import com.all.shared.messages.MessEngineConstants;
import com.all.shared.model.AllMessage;
import com.all.shared.model.User;
import com.all.shared.stats.AllStat;
import com.all.shared.stats.UserSpecs;
import com.all.shared.stats.usage.UserActionStat;
import com.all.shared.stats.usage.UserActions;
import com.all.ultrapeer.util.AllServerProxy;

@Service
@Deprecated
public class LoginService {

	private Log log = LogFactory.getLog(this.getClass());
	@Autowired
	private MessEngine messEngine;
	@Autowired
	private AllServerProxy userBackend;

	@MessageMethod(LOGIN_REQUEST_TYPE)
	@Deprecated
	public void processLoginRequest(AllMessage<LoginCommand> message) {
		LoginCommand loginCommand = message.getBody();
		log.info("Processing login request from " + loginCommand.getEmail());
		LoginResponse loginResponse = null;
		try {
			loginResponse = userBackend.postForObject("loginUrl", loginCommand, LoginResponse.class);
		} catch (Exception e) {
			loginResponse = new LoginResponse(LoginError.SERVER_DOWN);
		}
		if (loginResponse.isSuccessful()) {
			UserSpecs userSpecs = extractUserSpecs(message);
			reportLoginStats(userSpecs);
		}
		AllMessage<Serializable> result = new AllMessage<Serializable>(LOGIN_RESPONSE_TYPE, (Serializable) loginResponse);
		log.debug("login response : " + ToStringBuilder.reflectionToString(result));
		result.putProperty(NETWORKING_SESSION_ID, message.getProperty(NETWORKING_SESSION_ID));
		messEngine.send(result);
	}

	@MessageMethod(LOGIN_PASSWORD_REQUEST_TYPE)
	@Deprecated
	public void processForgotPasswordRequest(AllMessage<String> message) {
		log.info("Processing a forgot password request for user " + message.getBody());
		String forgotPasswordResult = userBackend.postForObject("forgotPasswordUrl", message.getBody(), String.class);
		log.debug("forgotPasswordResult: " + forgotPasswordResult);
		String result = "";
		String[] resultAsarray = forgotPasswordResult.split(";");
		if (resultAsarray.length == 2) {
			result = resultAsarray[0];
		} else {
			result = forgotPasswordResult;
		}
		AllMessage<String> responseMessage = new AllMessage<String>(LOGIN_PASSWORD_RESPONSE_TYPE, result);
		responseMessage.putProperty(NETWORKING_SESSION_ID, message.getProperty(NETWORKING_SESSION_ID));
		messEngine.send(responseMessage);
	}

	@MessageMethod(LOGIN_ABOUT_US_REQUEST_TYPE)
	@Deprecated
	public void registerAboutUsStat(String aboutUs) {
		userBackend.put("aboutUsUrl", aboutUs);
	}

	@MessageMethod(LOGIN_SIGNUP_REQUEST_TYPE)
	@Deprecated
	public void processSignupRequest(AllMessage<User> message) {
		log.info("Processing a signup request.");
		String result = userBackend.postForObject("signupUrl", message.getBody(), String.class);
		AllMessage<String> responseMessage = new AllMessage<String>(LOGIN_SIGNUP_RESPONSE_TYPE, result);
		responseMessage.putProperty(NETWORKING_SESSION_ID, message.getProperty(NETWORKING_SESSION_ID));
		messEngine.send(responseMessage);
	}


	@Deprecated
	private UserSpecs extractUserSpecs(AllMessage<LoginCommand> message) {
		UserSpecs userSpecs = new UserSpecs();
		LoginCommand loginCommand = message.getBody();
		userSpecs.setEmail(loginCommand.getEmail());
		userSpecs.setVersion(message.getProperty(MessEngineConstants.MP_SOURCE_ARTIFACT_VERSION));
		userSpecs.setOs(message.getProperty(MessEngineConstants.MP_SOURCE_OS));
		userSpecs.setOsVersion(message.getProperty(MessEngineConstants.MP_SOURCE_OS_VERSION));
		userSpecs.setJvm(message.getProperty(MessEngineConstants.MP_SOURCE_JAVA_VERSION));
		userSpecs.setScreenSize(message.getProperty(MessEngineConstants.MP_SOURCE_SCREEN_SIZE));
		userSpecs.setTimezone(message.getProperty(MessEngineConstants.MP_SOURCE_USER_TIMEZONE));
		userSpecs.setPublicIp(message.getProperty(MessEngineConstants.MP_SOURCE_PUBLIC_IP));
		return userSpecs;
	}

	@Deprecated
	private void reportLoginStats(UserSpecs userSpecs) {
		List<AllStat> userStats = new ArrayList<AllStat>();
		userStats.add(userSpecs);
		UserActionStat loginStat = new UserActionStat();
		loginStat.setAction(UserActions.AllNetwork.LOGIN);
		loginStat.setEmail(userSpecs.getEmail());
		loginStat.setTimes(1);
		userStats.add(loginStat);
		messEngine.send(new AllMessage<Collection<AllStat>>(USAGE_STATS_TYPE, userStats));
	}

}
