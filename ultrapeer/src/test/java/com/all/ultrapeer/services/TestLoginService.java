package com.all.ultrapeer.services;

import static com.all.networking.NetworkingConstants.NETWORKING_SESSION_ID;
import static com.all.shared.messages.MessEngineConstants.LOGIN_PASSWORD_REQUEST_TYPE;
import static com.all.shared.messages.MessEngineConstants.LOGIN_PASSWORD_RESPONSE_TYPE;
import static com.all.shared.messages.MessEngineConstants.LOGIN_REQUEST_TYPE;
import static com.all.shared.messages.MessEngineConstants.LOGIN_RESPONSE_TYPE;
import static com.all.shared.messages.MessEngineConstants.LOGIN_SIGNUP_REQUEST_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.all.messengine.Message;
import com.all.messengine.impl.StubMessEngine;
import com.all.shared.command.LoginCommand;
import com.all.shared.login.LoginError;
import com.all.shared.messages.ForgotPasswordResult;
import com.all.shared.messages.LoginResponse;
import com.all.shared.messages.MessEngineConstants;
import com.all.shared.model.AllMessage;
import com.all.shared.model.Avatar;
import com.all.shared.model.ContactInfo;
import com.all.shared.model.ContactRequest;
import com.all.shared.model.User;
import com.all.shared.stats.AllStat;
import com.all.ultrapeer.util.AllServerProxy;

@SuppressWarnings("deprecation")
public class TestLoginService {

	@InjectMocks
	private LoginService loginService = new LoginService();
	@Spy
	private StubMessEngine stubEngine = new StubMessEngine();
	@Mock
	private AllServerProxy userBackend;

	@Before
	public void shouldRegisterListener() throws Exception {
		MockitoAnnotations.initMocks(this);

		stubEngine.setup(loginService);

		assertNotNull(stubEngine.getMessageListeners(LOGIN_REQUEST_TYPE).get(0));
		assertNotNull(stubEngine.getMessageListeners(LOGIN_SIGNUP_REQUEST_TYPE).get(0));
		assertNotNull(stubEngine.getMessageListeners(LOGIN_PASSWORD_REQUEST_TYPE).get(0));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldProcessLoginRequest() throws Exception {
		String email = "loginSucceed@all.com";
		LoginCommand loginCommand = new LoginCommand(email, "password");
		AllMessage<LoginCommand> message = new AllMessage<LoginCommand>(LOGIN_REQUEST_TYPE, loginCommand);
		String sessionId = "1234567890";
		message.putProperty(NETWORKING_SESSION_ID, sessionId);
		LoginResponse loginResponse = new LoginResponse(LoginError.INVALID_CREDENTIALS);
		when(userBackend.postForObject(anyString(), anyObject(), isA(Class.class))).thenReturn(loginResponse);

		stubEngine.send(message);

		verify(userBackend).postForObject(anyString(), anyObject(), isA(Class.class));
		Message<?> responseMessage = stubEngine.getMessage(LOGIN_RESPONSE_TYPE);
		assertNotNull(responseMessage);
		assertEquals(sessionId, responseMessage.getProperty(NETWORKING_SESSION_ID));
		assertEquals(LoginError.INVALID_CREDENTIALS, ((LoginResponse) responseMessage.getBody()).getError());
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void shouldReportLoginSuccessfull() throws Exception {
		String email = "loginSucceed@all.com";
		LoginCommand loginCommand = new LoginCommand(email, "password");
		AllMessage<LoginCommand> message = new AllMessage<LoginCommand>(LOGIN_REQUEST_TYPE, loginCommand);
		String sessionId = "1234567890";
		message.putProperty(NETWORKING_SESSION_ID, sessionId);
		LoginResponse loginResponse = new LoginResponse();
		when(userBackend.postForObject(anyString(), anyObject(), isA(Class.class))).thenReturn(loginResponse);

		stubEngine.send(message);

		verify(userBackend).postForObject(anyString(), anyObject(), isA(Class.class));
		Message<?> responseMessage = stubEngine.getMessage(LOGIN_RESPONSE_TYPE);
		assertNotNull(responseMessage);
		assertTrue(((LoginResponse) responseMessage.getBody()).isSuccessful());
		AllMessage<Collection<AllStat>> statsMessage = (AllMessage<Collection<AllStat>>) stubEngine
				.getMessage(MessEngineConstants.USAGE_STATS_TYPE);
		assertNotNull(statsMessage);
		Collection<AllStat> stats = statsMessage.getBody();
		assertEquals(2, stats.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldReturnServerDownIfServerError() throws Exception {
		LoginCommand loginCommand = new LoginCommand("email", "password");
		AllMessage<LoginCommand> message = new AllMessage<LoginCommand>(LOGIN_REQUEST_TYPE, loginCommand);
		String sessionId = "1234567890";
		message.putProperty(NETWORKING_SESSION_ID, sessionId);
		when(userBackend.postForObject(anyString(), anyObject(), isA(Class.class))).thenThrow(
				new RuntimeException("Some unexpected server error."));

		stubEngine.send(message);

		verify(userBackend).postForObject(anyString(), anyObject(), isA(Class.class));
		Message<?> responseMessage = stubEngine.getMessage(LOGIN_RESPONSE_TYPE);
		assertNotNull(responseMessage);
		assertEquals(sessionId, responseMessage.getProperty(NETWORKING_SESSION_ID));
		assertEquals(LoginError.SERVER_DOWN, ((LoginResponse) responseMessage.getBody()).getError());
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void shouldSendPendingFriendshipRequestsOnFirstLogin() throws Exception {
		LoginCommand loginCommand = new LoginCommand("email", "password");
		AllMessage<LoginCommand> message = new AllMessage<LoginCommand>(LOGIN_REQUEST_TYPE, loginCommand);
		String sessionId = "1234567890";
		message.putProperty(NETWORKING_SESSION_ID, sessionId);
		User user = new User();
		Avatar avatar = new Avatar();
		LoginResponse loginResponse = new LoginResponse(user, avatar, "sessionId");
		loginResponse.setFirstLogin(true);
		List<ContactRequest> pendingRequests = new ArrayList<ContactRequest>();
		ContactRequest contactRequest = new ContactRequest(new ContactInfo(), new ContactInfo());
		contactRequest.setId(1L);
		pendingRequests.add(contactRequest);
		loginResponse.setPendingRequests(pendingRequests);
		when(userBackend.postForObject(anyString(), anyObject(), isA(Class.class))).thenReturn(loginResponse);

		stubEngine.send(message);

		verify(userBackend).postForObject(anyString(), anyObject(), isA(Class.class));
		Message<LoginResponse> responseMessage = (Message<LoginResponse>) stubEngine.getMessage(LOGIN_RESPONSE_TYPE);
		assertNotNull(responseMessage);
		LoginResponse actualResponse = responseMessage.getBody();
		assertTrue(actualResponse.isSuccessful());
		assertTrue(actualResponse.isFirstLogin());
		assertEquals(sessionId, responseMessage.getProperty(NETWORKING_SESSION_ID));
	}

	@Test
	public void shouldProcessSignupRequest() throws Exception {
		AllMessage<Serializable> message = new AllMessage<Serializable>(LOGIN_SIGNUP_REQUEST_TYPE, "1234567890");
		when(userBackend.postForObject(isA(String.class), any(Class.class), eq(String.class))).thenReturn("OK");
		String url = "signupUrl";

		stubEngine.send(message);

		verify(userBackend).postForObject(eq(url), any(Class.class), eq(String.class));
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void shouldProcessSignUpRequestForUrlAndOk() throws Exception {
		User user = mock(User.class);
		AllMessage<User> message = mock(AllMessage.class);
		when(message.getBody()).thenReturn(user);
		when(userBackend.postForObject("signupUrl", user, String.class)).thenReturn("OK");

		loginService.processSignupRequest(message);

	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void shouldNotProcessSignUpRequest() throws Exception {
		User user = mock(User.class);
		AllMessage<User> message = mock(AllMessage.class);
		when(message.getBody()).thenReturn(user);
		when(userBackend.postForObject("signupUrl", user, String.class)).thenReturn("SERVER_ERROR");

		loginService.processSignupRequest(message);

		((User) verify(user, never())).getFullName();
		((User) verify(user, never())).getEmail();
	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void shouldProcessForgotPasswordRequestOk() throws Exception {
		String email = "email";
		AllMessage<String> message = mock(AllMessage.class);
		when(message.getBody()).thenReturn(email);
		when(userBackend.postForObject("forgotPasswordUrl", email, String.class)).thenReturn("OK;http://someUrl");

		loginService.processForgotPasswordRequest(message);

	}

	@SuppressWarnings({ "unchecked" })
	@Test
	public void shouldNotProcessForgotPasswordRequestOk() throws Exception {
		String email = "email";
		AllMessage<String> message = mock(AllMessage.class);
		when(message.getBody()).thenReturn(email);
		when(userBackend.postForObject("forgotPasswordUrl", email, String.class)).thenReturn("SERVER_ERROR");

		loginService.processForgotPasswordRequest(message);

	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldProcessForgotPasswordRequest() throws Exception {
		String email = "some@all.com";
		AllMessage<String> request = new AllMessage<String>(LOGIN_PASSWORD_REQUEST_TYPE, email);
		String url = "forgotPasswordUrl";
		when(userBackend.postForObject(url, request.getBody(), String.class))
				.thenReturn(ForgotPasswordResult.OK.toString());

		stubEngine.send(request);

		verify(userBackend).postForObject(url, request.getBody(), String.class);
		AllMessage<String> responseMessage = (AllMessage<String>) stubEngine.getMessage(LOGIN_PASSWORD_RESPONSE_TYPE);
		assertNotNull(responseMessage);
		assertEquals(ForgotPasswordResult.OK, ForgotPasswordResult.valueOf(responseMessage.getBody()));
	}
}
