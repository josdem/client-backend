package com.all.ultrapeer.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.all.messengine.MessEngine;
import com.all.messengine.MessageMethod;
import com.all.peer.commons.messages.ForwardMessage;
import com.all.shared.messages.MessEngineConstants;
import com.all.shared.model.AllMessage;
import com.all.shared.model.ChatMessage;

@Service
@Deprecated
public class ChatService {

	@Autowired
	private MessEngine messEngine;

	@MessageMethod(MessEngineConstants.CHAT_MESSAGE_REQUEST)
	@Deprecated
	public void handleChatRequest(AllMessage<ChatMessage> message) {
		ChatMessage chatMessage = message.getBody();
		String forwardTo = chatMessage.getRecipient().getEmail();
		AllMessage<ChatMessage> response = new AllMessage<ChatMessage>(MessEngineConstants.CHAT_MESSAGE_RESPONSE,
				chatMessage);
		messEngine.send(new ForwardMessage(response, forwardTo));
	}

}
