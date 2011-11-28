package com.all.ultrapeer.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.all.messengine.Message;
import com.all.messengine.impl.StubMessEngine;
import com.all.peer.commons.messages.ForwardMessage;
import com.all.shared.messages.MessEngineConstants;
import com.all.shared.model.AllMessage;
import com.all.shared.model.ChatMessage;
import com.all.shared.model.ContactInfo;

@SuppressWarnings("deprecation")
public class TestChatService {
	@InjectMocks
	private ChatService service = new ChatService();
	@Spy
	private StubMessEngine stubEngine = new StubMessEngine();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		stubEngine.setup(service);
	}

	@SuppressWarnings( { "unchecked" })
	@Test
	public void shouldRedirectAChatMessageToTheDestinationAddingAPropertyToTheResponseMessageAndSendingItBackTruTheMessEngine()
			throws Exception {
		ContactInfo sender = new ContactInfo();
		sender.setId(1L);
		String forwardTo = "forwardHere";
		ContactInfo recipient = new ContactInfo();
		recipient.setEmail(forwardTo);
		ChatMessage chatMessage = new ChatMessage(sender, recipient, "hi");
		Message<ChatMessage> message = new AllMessage<ChatMessage>(MessEngineConstants.CHAT_MESSAGE_REQUEST, chatMessage);

		stubEngine.send(message);

		ForwardMessage redirectedMessage = (ForwardMessage) stubEngine.getMessage(ForwardMessage.TYPE);
		assertNotNull(redirectedMessage);
		assertEquals(forwardTo, redirectedMessage.getContactId());
		Message<ChatMessage> responseMessage = (Message<ChatMessage>) redirectedMessage.getBody();
		assertNotNull(responseMessage);
		assertEquals(MessEngineConstants.CHAT_MESSAGE_RESPONSE, responseMessage.getType());
	}

}
