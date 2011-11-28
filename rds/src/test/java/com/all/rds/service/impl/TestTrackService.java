package com.all.rds.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.MassIndexer;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.query.dsl.EntityContext;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.QueryContextBuilder;
import org.hibernate.search.query.dsl.TermContext;
import org.hibernate.search.query.dsl.TermMatchingContext;
import org.hibernate.search.query.dsl.TermTermination;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.all.rds.model.CachedTrack;
import com.all.rds.model.TrackUploadStatus;
import com.all.rds.service.ChunkStorageService;
import com.all.rds.service.impl.TrackServiceImpl.CreateLuceneIndexes;
import com.all.rds.service.impl.TrackServiceImpl.FullTextSessionfactory;
import com.all.shared.mc.TrackSearchResult;
import com.all.shared.mc.TrackStatus;
import com.all.shared.mc.TrackStatus.Status;
import com.all.shared.model.Track;

public class TestTrackService {

	@InjectMocks
	private TrackServiceImpl service = new TrackServiceImpl();
	@Mock
	private HibernateTemplate ht;
	@Mock
	private ChunkStorageService chunkStorageService;
	@Mock
	FullTextSessionfactory fullTextSessionfactory;
	@Mock
	private Session session;
	@Mock
	private Criteria criteria;
	@Mock
	private FullTextSession fullTextSession;
	@Captor
	private ArgumentCaptor<HibernateCallback<?>> callbackCaptor;

	private final String test = "testing";

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldSaveChunk() throws Exception {
		TrackUploadStatus status = Mockito.mock(TrackUploadStatus.class);
		when(ht.get(TrackUploadStatus.class, test)).thenReturn(status);
		int chunkId = 1;

		service.saveChunk(test, chunkId, test.getBytes());

		verify(chunkStorageService).put(test, 1, test.getBytes());
		verify(ht).saveOrUpdate(isA(TrackUploadStatus.class));
	}

	@Test
	public void shouldSaveLastChunkAndUpdateStatusToComplete() throws Exception {
		TrackUploadStatus status = Mockito.mock(TrackUploadStatus.class);
		when(ht.get(TrackUploadStatus.class, test)).thenReturn(status);

		service.saveChunk(test, TrackStatus.COMPLETE_UPLOAD, test.getBytes());

		verify(chunkStorageService).put(test, TrackStatus.COMPLETE_UPLOAD, test.getBytes());
		verify(ht).saveOrUpdate(isA(TrackUploadStatus.class));
		verify(status).setTrackStatus(Status.UPLOADED);
	}

	@Test
	public void shouldGetChunk() throws Exception {
		byte[] value = new byte[5];
		when(chunkStorageService.get(test, 1)).thenReturn(value);

		service.getChunk(test, 1);

		verify(chunkStorageService).get(test, 1);
	}

	@Test
	public void shouldNotUpdateChunkStatus() throws Exception {
		byte[] value = new byte[5];
		when(chunkStorageService.get(test, 1)).thenReturn(value);

		service.getChunk(test, 1);

		verify(ht, never()).get(TrackUploadStatus.class, test);
		verify(ht, never()).update(isA(TrackUploadStatus.class));
	}

	@Test
	public void shouldUpdateChunkStatus() throws Exception {
		byte[] value = "test".getBytes();
		when(chunkStorageService.get(test, 99)).thenReturn(value);
		TrackUploadStatus trackStatus = Mockito.mock(TrackUploadStatus.class);
		when(ht.get(TrackUploadStatus.class, test)).thenReturn(trackStatus);

		service.getChunk(test, 99);

		verify(ht).get(TrackUploadStatus.class, test);
		verify(ht).update(isA(TrackUploadStatus.class));
	}

	@Test
	public void shouldGetTrackStatus() throws Exception {
		TrackUploadStatus status = Mockito.mock(TrackUploadStatus.class);
		when(ht.get(TrackUploadStatus.class, test)).thenReturn(status);

		Object obj = service.getTrackStatus(test);

		verify(ht).get(eq(TrackUploadStatus.class), anyString());
		assertTrue(obj instanceof TrackStatus);
	}

	@Test
	public void shouldGetUnavailableTrackStatusIfNotFound() throws Exception {
		TrackStatus status = service.getTrackStatus(test);

		assertEquals(TrackStatus.Status.NOT_AVAILABLE, status.getTrackStatus());
		assertEquals(test, status.getTrackId());
	}

	@Test
	public void shouldCancelUpload() throws Exception {
		TrackUploadStatus status = Mockito.mock(TrackUploadStatus.class);
		when(ht.get(TrackUploadStatus.class, test)).thenReturn(status);

		service.cancelUpload(test);

		verify(ht).update(status);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldGetAvailableTracks() throws Exception {
		List<String> trackIds = Arrays.asList(new String[] { "trackA", "trackB" });

		service.filterTracksByAvailability(trackIds);
		verify(ht).executeFind(callbackCaptor.capture());
		HibernateCallback<List<String>> callback = (HibernateCallback<List<String>>) callbackCaptor.getValue();
		when(session.createCriteria(TrackUploadStatus.class)).thenReturn(criteria);
		callback.doInHibernate(session);
		verify(session).createCriteria(TrackUploadStatus.class);
		verify(criteria, times(2)).add(isA(Criterion.class));
	}

	@Test
	public void shouldStoreTrackMetadata() throws Exception {
		Track track = mock(Track.class);
		service.storeMetadata(track);
		verify(ht).saveOrUpdate(track);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldFindTracksByKeywords() throws Exception {
		String keyword = "SÓmë Keywörd";

		service.findTracksByKeyword(keyword);

		verify(ht).executeFind(callbackCaptor.capture());
		HibernateCallback<List<TrackSearchResult>> callback = (HibernateCallback<List<TrackSearchResult>>) callbackCaptor
				.getValue();

		Transaction transaction = mock(Transaction.class);
		SearchFactory searchFactory = mock(SearchFactory.class);
		QueryContextBuilder queryContextBuilder = mock(QueryContextBuilder.class);
		EntityContext entitycontext = mock(EntityContext.class);
		QueryBuilder queryBuilder = mock(QueryBuilder.class);
		TermContext termcontext = mock(TermContext.class);
		TermMatchingContext termMatchingContext = mock(TermMatchingContext.class);
		TermTermination termTermination = mock(TermTermination.class);
		org.apache.lucene.search.Query luceneQuery = mock(org.apache.lucene.search.Query.class);
		FullTextQuery fullTextQuery = mock(FullTextQuery.class);
		ArrayList<Object[]> resultList = new ArrayList<Object[]>();
		double score1 = 4.3;
		double score2 = 1.234;
		double score3 = 0.542;
		CachedTrack cachedTrack1 = new CachedTrack();
		CachedTrack cachedTrack2 = new CachedTrack();
		CachedTrack cachedTrack3 = new CachedTrack();
		resultList.add(new Object[] { cachedTrack1, new Float(score1) });
		resultList.add(new Object[] { cachedTrack2, new Float(score2) });
		resultList.add(new Object[] { cachedTrack3, new Float(score3) });

		when(fullTextSessionfactory.createFullTextSession(session)).thenReturn(fullTextSession);
		when(fullTextSession.beginTransaction()).thenReturn(transaction);
		when(fullTextSession.getSearchFactory()).thenReturn(searchFactory);
		when(searchFactory.buildQueryBuilder()).thenReturn(queryContextBuilder);
		when(queryContextBuilder.forEntity(CachedTrack.class)).thenReturn(entitycontext);
		when(entitycontext.get()).thenReturn(queryBuilder);
		when(queryBuilder.keyword()).thenReturn(termcontext);
		when(termcontext.onFields("album", "artist", "name")).thenReturn(termMatchingContext);
		when(termMatchingContext.matching(keyword)).thenReturn(termTermination);
		when(termTermination.createQuery()).thenReturn(luceneQuery);
		when(fullTextSession.createFullTextQuery(luceneQuery, CachedTrack.class)).thenReturn(fullTextQuery);
		when(fullTextQuery.list()).thenReturn(resultList);

		List<TrackSearchResult> doInHibernateResult = callback.doInHibernate(session);

		assertEquals(score1, doInHibernateResult.get(0).getScore(), 0.01);
		assertEquals(score2, doInHibernateResult.get(1).getScore(), 0.01);
		assertEquals(score3, doInHibernateResult.get(2).getScore(), 0.01);
		assertEquals(cachedTrack1, doInHibernateResult.get(0).getTrack());
		assertEquals(cachedTrack2, doInHibernateResult.get(1).getTrack());
		assertEquals(cachedTrack3, doInHibernateResult.get(2).getTrack());
		verify(fullTextQuery).setProjection(FullTextQuery.THIS, FullTextQuery.SCORE);
		verify(fullTextQuery).setMaxResults(400);
		verify(transaction).commit();
		verify(session).close();
	}

	@Test
	public void shouldInitializeLuceneIndex() throws Exception {
		CreateLuceneIndexes createLuceneIndexes = executeHibernateCallback();

		MassIndexer massIndexer = mock(MassIndexer.class);

		when(fullTextSession.createIndexer()).thenReturn(massIndexer);

		createLuceneIndexes.doInHibernate(session);

		verify(massIndexer).startAndWait();
	}

	@Test(expected = HibernateException.class)
	public void shouldThrowExceptionInInitializeLuceneIndex() throws Exception {
		CreateLuceneIndexes createLuceneIndexes = executeHibernateCallback();

		MassIndexer massIndexer = mock(MassIndexer.class);

		when(fullTextSession.createIndexer()).thenReturn(massIndexer);

		doThrow(new InterruptedException()).when(massIndexer).startAndWait();

		createLuceneIndexes.doInHibernate(session);
	}

	private CreateLuceneIndexes executeHibernateCallback() {
		ArgumentCaptor<CreateLuceneIndexes> createLuceneIndexesCaptor = ArgumentCaptor.forClass(CreateLuceneIndexes.class);

		service.initialize();

		verify(ht).execute(createLuceneIndexesCaptor.capture());

		CreateLuceneIndexes createLuceneIndexes = createLuceneIndexesCaptor.getValue();

		when(fullTextSessionfactory.createFullTextSession(session)).thenReturn(fullTextSession);
		return createLuceneIndexes;
	}
}
