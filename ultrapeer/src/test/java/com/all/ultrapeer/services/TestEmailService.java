package com.all.ultrapeer.services;

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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.all.messengine.impl.StubMessEngine;
import com.all.shared.messages.MessEngineConstants;
import com.all.shared.model.AllMessage;
import com.all.shared.model.PendingEmail;
import com.all.ultrapeer.util.AllServerProxy;

public class TestEmailService {

	@InjectMocks
	@SuppressWarnings("deprecation")
	private EmailService service = new EmailService();
	@Spy
	private StubMessEngine messEngine = new StubMessEngine();
	@Mock
	private AllServerProxy userBackend;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		messEngine.setup(service);
	}

	@Test
	public void shouldSendEmail() throws Exception {
		final PendingEmail pendingEmail = new PendingEmail();
		AllMessage<PendingEmail> emailMessage = new AllMessage<PendingEmail>(MessEngineConstants.SEND_EMAIL_TYPE,
				pendingEmail);
		final Long id = 1L;
		when(userBackend.postForObject(anyString(), eq(pendingEmail), eq(PendingEmail.class))).thenAnswer(
				new Answer<PendingEmail>() {

					@Override
					public PendingEmail answer(InvocationOnMock invocation) throws Throwable {
						pendingEmail.setId(id);
						return pendingEmail;
					}
				});

		messEngine.send(emailMessage);

		assertNotNull(pendingEmail.getId());
		assertNotNull(((StubMessEngine) messEngine).getMessage(MessEngineConstants.PUSH_PENDING_EMAIL_TYPE));
	}

	@Test
	public void shouldNotSendEmail() throws Exception {
		PendingEmail pendingEmail = new PendingEmail();
		pendingEmail.setFromMail("josdem@all.com");
		AllMessage<PendingEmail> emailMessage = new AllMessage<PendingEmail>(MessEngineConstants.SEND_EMAIL_TYPE,
				pendingEmail);
		when(userBackend.postForObject(anyString(), eq(pendingEmail), eq(PendingEmail.class))).thenReturn(pendingEmail);

		messEngine.send(emailMessage);

		verify(userBackend).postForObject(anyString(), eq(pendingEmail), eq(PendingEmail.class));
	}
}
