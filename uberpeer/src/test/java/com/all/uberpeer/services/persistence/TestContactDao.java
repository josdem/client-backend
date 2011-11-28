package com.all.uberpeer.services.persistence;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.hibernate.Query;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.all.shared.model.ContactInfo;
import com.all.shared.model.User;
import com.all.uberpeer.persistence.ContactDao;

public class TestContactDao {

	@InjectMocks
	private ContactDao contactDao = new ContactDao();
	@Mock
	private HibernateTemplate ht;
	@Mock
	private Session session;
	@Mock
	private Query query;
	@Captor
	private ArgumentCaptor<HibernateCallback<?>> callbackCaptor;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldFindContactByEmail() throws Exception {
		String email = "user@all.com";
		
		contactDao.findContactByEmail(email);
		
		verify(ht).execute(callbackCaptor.capture());

		HibernateCallback<?> callback = callbackCaptor.getValue();
		when(query.uniqueResult()).thenReturn(new User());
		when(session.createQuery(anyString())).thenReturn(query);

		Object result = callback.doInHibernate(session);
		
		verify(query).setString("email", email);
		assertNotNull(result);
		assertTrue(result instanceof ContactInfo);
	}

	@Test
	public void shouldReturnNullWhenCannotFindContactByEmail() throws Exception {
		String email = "user@all.com";
		
		contactDao.findContactByEmail(email);
		
		verify(ht).execute(callbackCaptor.capture());

		HibernateCallback<?> callback = callbackCaptor.getValue();
		when(session.createQuery(anyString())).thenReturn(query);

		Object result = callback.doInHibernate(session);
		
		verify(query).setString("email", email);
		assertNull(result);
	}

}
