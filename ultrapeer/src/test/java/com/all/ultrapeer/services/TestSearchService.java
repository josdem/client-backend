package com.all.ultrapeer.services;

import static com.all.networking.NetworkingConstants.NETWORKING_SESSION_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

import com.all.messengine.Message;
import com.all.messengine.impl.StubMessEngine;
import com.all.shared.messages.MessEngineConstants;
import com.all.shared.model.AllMessage;
import com.all.shared.model.ContactInfo;
import com.all.ultrapeer.util.AllServerProxy;

public class TestSearchService {

	@InjectMocks
	@SuppressWarnings("deprecation")
	private SearchService service = new SearchService();
	@Spy
	private StubMessEngine stubEngine = new StubMessEngine();
	@Mock
	private AllServerProxy userBackend;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		stubEngine.setup(service);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldProcessSearchRequestMessage() throws Exception {
		String keyword = "keyword";
		String sessionId = "someSession";

		AllMessage<String> requestMessage = new AllMessage<String>(MessEngineConstants.SEARCH_CONTACTS_REQUEST_TYPE,
				keyword);
		requestMessage.putProperty(NETWORKING_SESSION_ID, sessionId);

		List<ContactInfo> response = new ArrayList<ContactInfo>();
		when(userBackend.getForCollection(anyString(), any(Class.class), any(Class.class), eq(keyword))).thenReturn(
				response);

		stubEngine.send(requestMessage);

		verify(userBackend).getForCollection(anyString(), eq(ArrayList.class), eq(ContactInfo.class), eq(keyword));
		Message<?> responseMessage = stubEngine.getMessage(MessEngineConstants.SEARCH_CONTACTS_RESPONSE_TYPE);
		assertNotNull(responseMessage);
		assertEquals(sessionId, responseMessage.getProperty(NETWORKING_SESSION_ID));
	}

}
