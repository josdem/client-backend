package com.all.rds.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

import com.all.rds.model.CachedTrack;
import com.all.rds.model.Category;
import com.all.rds.model.TopPlaylist;


public class TestTopHundredServiceImpl {

	@InjectMocks
	private TopHundredServiceImpl service = new TopHundredServiceImpl();
	@Mock
	private HibernateTemplate hibernateTemplate;
	@Captor
	private ArgumentCaptor<HibernateCallback<?>> callbackCaptor;
	@Mock
	private Session session;
	@Mock
	private Query query;
	@Mock
	private Category category;
	
	@Before
	public void setup(){
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void shouldGetCategories(){
		List<Category> categories = new ArrayList<Category>();
		
		when(hibernateTemplate.loadAll(Category.class)).thenReturn(categories );
		
		assertSame(categories, service.getCategories());
		
	}
	
	@Test
	public void shouldGetTopPlaylistsForValidCategory() throws Exception {
		Long categoryId = 1L;
		service.getTopPlaylists(categoryId );
		
		verify(hibernateTemplate).executeFind(callbackCaptor.capture());
		
		HibernateCallback<?> callback = callbackCaptor.getValue();
		assertNotNull(callback);
		when(session.createQuery(anyString())).thenReturn(query);
		when(query.uniqueResult()).thenReturn(category);
		List<TopPlaylist> expected = new ArrayList<TopPlaylist>();
		when(category.list()).thenReturn(expected);
		
		assertSame(expected, callback.doInHibernate(session));
		verify(query).setParameter("categoryId", categoryId);
	}
	
	@Test
	public void shouldReturnEmptyListForInvalidCategory() throws Exception {
		Long categoryId = 1L;
		service.getTopPlaylists(categoryId );
		
		verify(hibernateTemplate).executeFind(callbackCaptor.capture());
		
		HibernateCallback<?> callback = callbackCaptor.getValue();
		assertNotNull(callback);
		when(session.createQuery(anyString())).thenReturn(query);

		assertEquals(Collections.emptyList(), callback.doInHibernate(session));
		verify(query).setParameter("categoryId", categoryId);		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void shouldGetTracksForTopPlaylist() throws Exception {
		String playlistId = "playlistId";
		service.getTracks(playlistId);
		
		verify(hibernateTemplate).executeFind(callbackCaptor.capture());
		
		HibernateCallback<?> callback = callbackCaptor.getValue();
		assertNotNull(callback);
		when(session.createQuery(anyString())).thenReturn(query);
		List tracks = new ArrayList<CachedTrack>();
		when(query.list()).thenReturn(tracks);
		when(category.list()).thenReturn(tracks);
		
		assertSame(tracks, callback.doInHibernate(session));
		verify(query).setParameter("topPlaylistId", playlistId);
		
	}
	
	@Test
	public void shuldGetRandomPlaylist() throws Exception {
		TopPlaylist topPlaylist = mock(TopPlaylist.class);
		String playlistId = "playlistId";
		when(topPlaylist.getHashcode()).thenReturn(playlistId);
		service.getRandomTopPlaylist();
		
		verify(hibernateTemplate).execute(callbackCaptor.capture());
		
		HibernateCallback<?> callback = callbackCaptor.getValue();
		assertNotNull(callback);
		when(session.createQuery(anyString())).thenReturn(query);
		List tracks = new ArrayList<CachedTrack>();
		when(query.uniqueResult()).thenReturn(topPlaylist);
		when(query.list()).thenReturn(tracks);
		when(category.list()).thenReturn(tracks);
		
		assertSame(topPlaylist, callback.doInHibernate(session));
		verify(query).setMaxResults(1);
		verify(query).uniqueResult();
		verify(query).setParameter("topPlaylistId", playlistId);		
		verify(query).list();
		verify(topPlaylist).setTracks(tracks);
	}
}
