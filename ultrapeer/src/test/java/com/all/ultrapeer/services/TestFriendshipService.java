package com.all.ultrapeer.services;

import static com.all.shared.messages.MessEngineConstants.FRIENDSHIP_REQUEST_RESULT_TYPE;
import static com.all.shared.messages.MessEngineConstants.FRIENDSHIP_REQUEST_TYPE;
import static com.all.shared.messages.MessEngineConstants.FRIENDSHIP_RESPONSE_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.all.messengine.Message;
import com.all.messengine.impl.StubMessEngine;
import com.all.shared.messages.FriendshipRequestStatus;
import com.all.shared.model.AllMessage;
import com.all.shared.model.ContactInfo;
import com.all.shared.model.ContactRequest;
import com.all.ultrapeer.util.AllServerProxy;

public class TestFriendshipService {

	@SuppressWarnings("deprecation")
	@InjectMocks
	private FriendshipService service = new FriendshipService();
	@Spy
	private StubMessEngine testEngine = new StubMessEngine();
	@Mock
	private AllServerProxy userBackend;

	@Mock
	private ContactInfo requester;
	@Mock
	private ContactInfo requested;
	private ContactRequest contactRequest;
	private long requesterId;
	private long requestedId;

	@Before
	public void init() throws Exception {
		MockitoAnnotations.initMocks(this);
		testEngine.setup(service);

		contactRequest = new ContactRequest(requester, requested);
		contactRequest.setId(100L);
		requesterId = 1L;
		when(requester.getId()).thenReturn(requesterId);
		requestedId = 2L;
		when(requested.getId()).thenReturn(requestedId);

	}

	@Test
	public void shouldProcessFriendshipRequest() throws Exception {
		long contactRequestId = 5L;
		when(userBackend.getForObject(anyString(), eq(String.class), eq(requesterId), eq(requestedId))).thenReturn(
				"" + contactRequestId);

		testEngine.send(new AllMessage<ContactRequest>(FRIENDSHIP_REQUEST_TYPE, contactRequest));

		verify(userBackend).getForObject(anyString(), eq(String.class), eq(requesterId), eq(requestedId));
		assertNotNull(testEngine.getMessage(FRIENDSHIP_REQUEST_TYPE));
		assertNotNull(testEngine.getMessage(FRIENDSHIP_REQUEST_RESULT_TYPE));
	}

	@Test
	public void shouldCreateFriendshipResponseAlertInCaseOfReciprocalInvitations() throws Exception {
		contactRequest.setId(null);

		when(userBackend.getForObject(anyString(), eq(String.class), eq(requesterId), eq(requestedId))).thenReturn(
				FriendshipRequestStatus.RECIPROCAL_INVITATION.toString());

		testEngine.send(new AllMessage<ContactRequest>(FRIENDSHIP_REQUEST_TYPE, contactRequest));

		verify(userBackend).getForObject(anyString(), eq(String.class), eq(requesterId), eq(requestedId));
		Message<?> responseMessage = testEngine.getMessage(FRIENDSHIP_REQUEST_RESULT_TYPE);
		assertNotNull(responseMessage);
		assertEquals(FriendshipRequestStatus.RECIPROCAL_INVITATION.toString(), responseMessage.getBody());
		// 4 Messages: The request, 2 FriendshipResponseAlerts and the friendship request result response.
		assertEquals(2, testEngine.sentMessagesCount());
	}

	@Test
	public void shouldCreateFriendshipResponseAlertInCaseOfRequesterInvitation() throws Exception {
		contactRequest.setId(null);

		when(userBackend.getForObject(anyString(), eq(String.class), eq(requesterId), eq(requestedId))).thenReturn(
				FriendshipRequestStatus.REACTIVATED_FRIENDSHIP.toString());

		testEngine.send(new AllMessage<ContactRequest>(FRIENDSHIP_REQUEST_TYPE, contactRequest));

		verify(userBackend).getForObject(anyString(), eq(String.class), eq(requesterId), eq(requestedId));
		Message<?> responseMessage = testEngine.getMessage(FRIENDSHIP_REQUEST_RESULT_TYPE);
		assertNotNull(responseMessage);
		assertEquals(FriendshipRequestStatus.REACTIVATED_FRIENDSHIP.toString(), responseMessage.getBody());
		// 3 Messages: The request, 2 FriendshipResponseAlerts and the friendship request result response.
		assertEquals(2, testEngine.sentMessagesCount());
	}

	@Test
	public void shouldProcessFriendshipResponse() throws Exception {
		contactRequest.accept();

		testEngine.send(new AllMessage<ContactRequest>(FRIENDSHIP_RESPONSE_TYPE, contactRequest));
		Thread.sleep(300);

		verify(userBackend).put(anyString(), eq("true"), eq(contactRequest.getId()));
	}

}
