package com.all.ultrapeer.services;

import static com.all.ultrapeer.messages.UltrapeerMessages.Types.START_ULTRAPEER_SERVICES_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.all.messengine.impl.StubMessEngine;
import com.all.networking.NetworkingConstants;
import com.all.shared.messages.MessEngineConstants;
import com.all.shared.model.AllMessage;
import com.all.shared.model.ContactInfo;
import com.all.ultrapeer.util.AllServerProxy;

public class TestDefaultContactsService {

	@InjectMocks
	@SuppressWarnings("deprecation")
	private DefaultContactsService service = new DefaultContactsService();
	@Spy
	private StubMessEngine messEngine = new StubMessEngine();
	@Mock
	private AllServerProxy userBackend;
	@Mock
	private ContactInfo defaultContact;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		messEngine.setup(service);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void shouldUpdateDefaulContactsListWhenServiceStarts() throws Exception {
		startService();
		verify(userBackend).getForCollection(anyString(), any(Class.class), eq(ContactInfo.class));
	}

	private void startService() {
		messEngine.send(new AllMessage<String>(START_ULTRAPEER_SERVICES_TYPE, ""));

	}

	@Test
	public void shouldRespondToDefaultContactsRequests() throws Exception {
		List<ContactInfo> defaultContacts = getDefaultContacts();
		assertTrue(defaultContacts.contains(defaultContact));
	}

	@SuppressWarnings("unchecked")
	private List<ContactInfo> getDefaultContacts() throws Exception {
		setBackendExpectations();
		startService();
		AllMessage<String> request = sendDefaultContactsRequest();

		AllMessage<List<ContactInfo>> response = (AllMessage<List<ContactInfo>>) messEngine
				.getMessage(MessEngineConstants.DEFAULT_CONTACTS_RESPONSE_TYPE);
		assertNotNull(response);
		assertEquals(request.getProperty(NetworkingConstants.NETWORKING_SESSION_ID), response
				.getProperty(NetworkingConstants.NETWORKING_SESSION_ID));
		return response.getBody();
	}

	private AllMessage<String> sendDefaultContactsRequest() {
		AllMessage<String> request = new AllMessage<String>(MessEngineConstants.DEFAULT_CONTACTS_REQUEST_TYPE,
				"user@all.com");
		request.putProperty(NetworkingConstants.NETWORKING_SESSION_ID, "sessionId");
		messEngine.send(request);
		return request;
	}

	@SuppressWarnings("unchecked")
	private void setBackendExpectations() {
		List<ContactInfo> contacts = new ArrayList<ContactInfo>();
		contacts.add(defaultContact);
		when(userBackend.getForCollection(anyString(), any(Class.class), eq(ContactInfo.class))).thenReturn(contacts);
	}

}
