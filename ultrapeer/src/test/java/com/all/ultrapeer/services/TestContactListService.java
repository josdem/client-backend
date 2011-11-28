package com.all.ultrapeer.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.all.messengine.Message;
import com.all.messengine.impl.StubMessEngine;
import com.all.networking.NetworkingConstants;
import com.all.shared.messages.MessEngineConstants;
import com.all.shared.model.AllMessage;
import com.all.shared.model.ContactInfo;
import com.all.ultrapeer.util.AllServerProxy;

public class TestContactListService {

	@InjectMocks
	private ContactListService service = new ContactListService();
	@Spy
	private StubMessEngine messEngine = new StubMessEngine();
	@Mock
	private AllServerProxy restBackend;

	String folderId = "folderId";
	Class<?> byteArrayClass = new byte[] {}.getClass();
	String someUrl = "someBackendUrl";

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		messEngine.setup(service);
	}

	@Test
	public void shouldProcessDeleteContactsMessage() throws Exception {
		String body = "contactIds";
		String sender = "sender";
		AllMessage<String> message = new AllMessage<String>(MessEngineConstants.DELETE_CONTACTS_TYPE, body);
		message.putProperty(MessEngineConstants.SENDER_ID, sender);

		messEngine.send(message);

		verify(restBackend).delete(anyString(), eq(sender), eq(body));
	}

	@Test
	public void shouldProceesDeletePendingEmailMessage() throws Exception {
		String body = "contactIds";
		AllMessage<String> message = new AllMessage<String>(MessEngineConstants.DELETE_PENDING_EMAILS_TYPE, body);

		messEngine.send(message);

		verify(restBackend).delete(anyString(), eq(body));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void shouldProcessUpdateContactProfileRequest() throws Exception {
		ContactInfo contact = new ContactInfo();
		contact.setEmail("user@all.com");
		AllMessage<ContactInfo> request = new AllMessage<ContactInfo>(MessEngineConstants.UPDATE_CONTACT_PROFILE_REQUEST, contact );
		String sessionId = "sessionId";
		request.putProperty(NetworkingConstants.NETWORKING_SESSION_ID, sessionId);
	
		messEngine.send(request);

		verify(restBackend).postForObject(anyString(), eq(contact), eq(ContactInfo.class));
		Message<?> response = messEngine.getMessage(MessEngineConstants.UPDATE_CONTACT_PROFILE_RESPONSE);
		assertNotNull(response);
		assertEquals(sessionId, response.getProperty(NetworkingConstants.NETWORKING_SESSION_ID));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void shouldProcessContactListRequest() throws Exception {
		String email = "user@all.com";
		AllMessage<String> request = new AllMessage<String>(MessEngineConstants.CONTACT_LIST_REQUEST_TYPE, email );
		String sessionId = "session Id";
		request.putProperty(NetworkingConstants.NETWORKING_SESSION_ID, sessionId);
		when(restBackend.getForCollection(anyString(), any(Class.class), eq(ContactInfo.class), eq(email))).thenReturn(new ArrayList<ContactInfo>());
		
		
		messEngine.send(request);
		
		verify(restBackend).getForCollection(anyString(), any(Class.class), eq(ContactInfo.class), eq(email));
		Message<?> response = messEngine.getMessage(MessEngineConstants.CONTACT_LIST_RESPONSE_TYPE);
		assertNotNull(response);
		assertEquals(sessionId , response.getProperty(NetworkingConstants.NETWORKING_SESSION_ID));
	}
	
}
