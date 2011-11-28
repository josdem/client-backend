package com.all.ultrapeer.services;

import static com.all.networking.NetworkingConstants.MESSAGE_SENDER;
import static com.all.networking.NetworkingConstants.NETWORKING_SESSION_ID;
import static com.all.shared.messages.MessEngineConstants.IMPORT_CONTACTS_REQUEST_TYPE;
import static com.all.shared.messages.MessEngineConstants.IMPORT_CONTACTS_RESPONSE_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.all.messengine.impl.StubMessEngine;
import com.all.shared.command.LoginCommand;
import com.all.shared.external.email.EmailDomain;
import com.all.shared.messages.CrawlerResponse;
import com.all.shared.messages.EmailContact;
import com.all.shared.model.AllMessage;
import com.all.shared.model.ContactInfo;
import com.all.ultrapeer.util.AllServerProxy;

public class TestCrawlerService {

	private static final String SENDER_MAIL = "sender@gmail";

	@InjectMocks
	@SuppressWarnings("deprecation")
	private CrawlerService service = new CrawlerService();

	@Spy
	private StubMessEngine testEngine = new StubMessEngine();
	@Mock
	private AllServerProxy restBackend;

	private String sessionId = "sessionId";
	private CrawlerResponse response;

	@SuppressWarnings({ "unchecked", "deprecation" })
	@Before
	public void addListeners() throws Exception {
		MockitoAnnotations.initMocks(this);
		testEngine.setup(service);

		response = new CrawlerResponse();
		ContactInfo contact = new ContactInfo();
		contact.setName("name");
		List<ContactInfo> registeredContacts = Arrays.asList(new ContactInfo[] { contact });
		EmailContact emailContact = new EmailContact(SENDER_MAIL, new HashMap<String, String>(), registeredContacts);
		response.addEmailContact(emailContact);
		when(restBackend.postForObject(anyString(), anyObject(), isA(Class.class))).thenReturn(response);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldProcessImportContactRequestMessage() throws Exception {
		HashMap<EmailDomain, List<LoginCommand>> messageBody = new HashMap<EmailDomain, List<LoginCommand>>();
		AllMessage<HashMap<EmailDomain, List<LoginCommand>>> requestMessage = new AllMessage<HashMap<EmailDomain, List<LoginCommand>>>(
				IMPORT_CONTACTS_REQUEST_TYPE, messageBody);
		requestMessage.putProperty(NETWORKING_SESSION_ID, sessionId);
		String sender = "sender";
		requestMessage.putProperty(MESSAGE_SENDER, sender);
		testEngine.send(requestMessage);

		assertEquals(2, testEngine.sentMessagesCount());
		AllMessage<CrawlerResponse> responseMessage = (AllMessage<CrawlerResponse>) testEngine
				.getMessage(IMPORT_CONTACTS_RESPONSE_TYPE);
		assertEquals(IMPORT_CONTACTS_RESPONSE_TYPE, responseMessage.getType());
		assertEquals(sessionId, responseMessage.getProperty(NETWORKING_SESSION_ID));
		CrawlerResponse body = responseMessage.getBody();
		assertNotNull(body);
	}
}
