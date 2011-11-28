package com.all.ultrapeer.messages;

public interface UltrapeerMessages {

	interface Types {
		String START_ULTRAPEER_SERVICES_TYPE = "START_ULTRAPEER_SERVICES";
		String STOP_ULTRAPEER_SERVICES_TYPE = "STOP_ULTRAPEER_SERVICES";
		String USER_PRESENCE_EXPIRED_TYPE = "USER_PRESENCE_EXPIRED_TYPE";
		String REQUEST_USER_NOTIFICATIONS_TYPE = "REQUEST_USER_NOTIFICATIONS";
	}

	interface Properties {
		String MP_UNWRAP_BEFORE_SEND = "MP_UNWRAP_BEFORE_SEND";
	}

}
