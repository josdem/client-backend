package com.all.ultrapeer.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.all.messengine.Message;
import com.all.messengine.impl.StubMessEngine;
import com.all.networking.NetworkingConstants;
import com.all.shared.alert.Alert;
import com.all.shared.messages.MessEngineConstants;
import com.all.shared.model.AllMessage;
import com.all.shared.model.ContactInfo;
import com.all.ultrapeer.util.AllServerProxy;

public class TestAlertService {

	@InjectMocks
	private AlertService service = new AlertService();
	@Spy
	private StubMessEngine stubEngine = new StubMessEngine();
	@Mock
	private Alert alert;
	@Mock
	private ContactInfo receiver;
	@Mock
	private ContactInfo sender;
	@Mock
	private AllServerProxy allBackend;

	private String receiverEmail = "somebody@all.com";

	private String alertId = "alertId";

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		stubEngine.setup(service);
		Set<String> registeredTypes = stubEngine.getRegisteredTypes();
		assertTrue(registeredTypes.contains(MessEngineConstants.PUT_ALERT_TYPE));
		assertTrue(registeredTypes.contains(MessEngineConstants.DELETE_ALERT_TYPE));
		assertTrue(registeredTypes.contains(MessEngineConstants.ALERTS_REQUEST_TYPE));

		when(alert.getReceiver()).thenReturn(receiver);
		when(alert.getSender()).thenReturn(sender);
		when(alert.getTypedClass()).thenReturn(Alert.class);
		when(alert.getId()).thenReturn(alertId);
		when(receiver.getEmail()).thenReturn(receiverEmail);
	}

	@Test
	public void shouldProcessPutAlertMessage() throws Exception {
		stubEngine.send(new AllMessage<Alert>(MessEngineConstants.PUT_ALERT_TYPE, alert));

		verify(allBackend).put(AlertService.SAVE_ALERT, alert, alert.getId());
	}

	@Test
	public void shouldDeleteAlert() throws Exception {
		stubEngine.send(new AllMessage<Alert>(MessEngineConstants.DELETE_ALERT_TYPE, alert));

		verify(allBackend).delete(AlertService.DELETE_ALERT, alert.getId());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void shouldProcessAlertRequestMessage() throws Exception {
		String networkingSessionIdValue = "networkingSessionIdValue";

		AllMessage<String> requestMessage = new AllMessage<String>(MessEngineConstants.ALERTS_REQUEST_TYPE, receiverEmail);
		requestMessage.putProperty(NetworkingConstants.NETWORKING_SESSION_ID, networkingSessionIdValue);
		stubEngine.send(requestMessage);

		verify(allBackend).getForCollection(AlertService.RETRIEVE_ALERTS, ArrayList.class, Alert.class, receiverEmail);

		Message<?> responseMessage = stubEngine.getMessage(MessEngineConstants.ALERTS_RESPONSE_TYPE);
		assertNotNull(responseMessage);
		assertEquals(networkingSessionIdValue, responseMessage.getProperty(NetworkingConstants.NETWORKING_SESSION_ID));
	}

}
