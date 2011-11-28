package com.all.ultrapeer.services;

import static com.all.networking.NetworkingConstants.NETWORKING_SESSION_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
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
import org.springframework.http.HttpMethod;

import com.all.messengine.Message;
import com.all.messengine.impl.StubMessEngine;
import com.all.shared.messages.MessEngineConstants;
import com.all.shared.model.AllMessage;
import com.all.shared.model.Avatar;
import com.all.shared.model.User;
import com.all.ultrapeer.util.AllServerProxy;

public class TestUserProfileService {

	@InjectMocks
	private UserProfileService service = new UserProfileService();

	@Spy
	private StubMessEngine testEngine = new StubMessEngine();
	@Mock
	private AllServerProxy userBackend;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		testEngine.setup(service);

	}

	@Test
	public void shouldUpdateUserProfile() throws Exception {
		User messageBody = new User();

		testEngine.send(new AllMessage<User>(MessEngineConstants.UPDATE_USER_PROFILE_TYPE, messageBody));

		verify(userBackend).send(eq("updateProfileUrl"), eq(HttpMethod.PUT), eq(messageBody), any(Object[].class),
				any(String[][].class));
	}

	@Test
	public void shouldProcessUpdateAvatarMessage() throws Exception {
		Avatar avatar = new Avatar();
		AllMessage<Avatar> message = new AllMessage<Avatar>(MessEngineConstants.UPDATE_USER_AVATAR_TYPE, avatar);

		testEngine.send(message);

		verify(userBackend).send(anyString(), eq(HttpMethod.PUT), eq(avatar), any(Object[].class), any(String[][].class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldProcessAvatarRequestMessage() throws Exception {

		String contactId = "id";
		AllMessage<String> message = new AllMessage<String>(MessEngineConstants.AVATAR_REQUEST_TYPE, contactId);
		String expectedOwner = "somebody@all.com";
		message.putProperty(MessEngineConstants.AVATAR_OWNER, expectedOwner);
		String expectedSession = "expectedId";
		message.putProperty(NETWORKING_SESSION_ID, expectedSession);
		when(userBackend.getForObject(anyString(), any(Class.class), eq(contactId))).thenReturn(new Avatar());

		testEngine.send(message);

		verify(userBackend).getForObject(anyString(), any(Class.class), eq(message.getBody()));
		Message<?> responseMessage = testEngine.getMessage(MessEngineConstants.AVATAR_RESPONSE_TYPE);
		assertNotNull(responseMessage);
		assertEquals(expectedOwner, responseMessage.getProperty(MessEngineConstants.AVATAR_OWNER));
		assertEquals(expectedSession, responseMessage.getProperty(NETWORKING_SESSION_ID));
	}

	@Test
	public void shouldProcessUpdateQuoteMessage() throws Exception {
		String quote = "userQuote";
		AllMessage<String> message = new AllMessage<String>(MessEngineConstants.UPDATE_USER_QUOTE_TYPE, quote);
		String userId = "1";
		message.putProperty(MessEngineConstants.SENDER_ID, userId);

		testEngine.send(message);

		verify(userBackend).send(anyString(), eq(HttpMethod.PUT), eq(quote), any(Object[].class), any(String[][].class));

	}

}
