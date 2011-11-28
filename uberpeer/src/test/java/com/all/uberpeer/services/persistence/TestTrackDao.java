package com.all.uberpeer.services.persistence;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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

import com.all.backend.commons.media.MusicTrack;
import com.all.uberpeer.persistence.TrackDao;

public class TestTrackDao {
	
	@InjectMocks
	private TrackDao trackDao = new TrackDao();
	
	@Mock
	private HibernateTemplate ht;
	
	@Mock
	private Query query;
	
	@Mock
	private Session session;
	
	@Captor
	private ArgumentCaptor<HibernateCallback<?>> callbackCaptor;

	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
	}
	@Test
	public void shouldFindTracksByHashcode() throws Exception {
		String hashcode = "hashcode";
		trackDao.findTrackByHashcode(hashcode);
		
		verify(ht).execute(callbackCaptor.capture());
		
		HibernateCallback<?> callback = callbackCaptor.getValue();
		
		when(query.uniqueResult()).thenReturn(new MusicTrack());
		when(session.createQuery(anyString())).thenReturn(query);
		
		Object result = callback.doInHibernate(session);
		
		verify(query).setString("hashcode", hashcode);
		assertNotNull(result);
		assertTrue(result instanceof MusicTrack);
		
		
	}

}
