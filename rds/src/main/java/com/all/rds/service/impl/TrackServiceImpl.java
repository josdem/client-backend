package com.all.rds.service.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.all.rds.model.CachedTrack;
import com.all.rds.model.TrackUploadStatus;
import com.all.rds.service.ChunkStorageService;
import com.all.rds.service.TrackService;
import com.all.shared.mc.TrackSearchResult;
import com.all.shared.mc.TrackStatus;
import com.all.shared.mc.TrackStatus.Status;
import com.all.shared.model.Track;

@Service
public class TrackServiceImpl implements TrackService {

	private final static Log LOG = org.apache.commons.logging.LogFactory.getLog(TrackServiceImpl.class);
	@Autowired
	private HibernateTemplate ht;
	@Autowired
	private ChunkStorageService chunkStorageService;

	private FullTextSessionfactory fullTextSessionfactory = new FullTextSessionfactory();

	@PostConstruct
	public void initialize() {
		ht.execute(new CreateLuceneIndexes());
	}

	@Override
	public void saveChunk(String trackId, int chunkId, byte[] chunkData) {
		chunkStorageService.put(trackId, chunkId, chunkData);
		TrackUploadStatus status = (TrackUploadStatus) getTrackStatus(trackId);
		status.setTrackStatus(Status.UPLOADING);
		status.setLastChunkNumber(chunkId);
		if (chunkId == TrackStatus.COMPLETE_UPLOAD) {
			status.setTrackStatus(Status.UPLOADED);
			status.setUploadedOn(new Date());
		}
		ht.saveOrUpdate(status);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> filterTracksByAvailability(final List<String> trackIds) {
		return ht.executeFind(new HibernateCallback<List<String>>() {
			@Override
			public List<String> doInHibernate(Session session) throws HibernateException, SQLException {
				Criteria criteria = session.createCriteria(TrackUploadStatus.class);
				criteria.setProjection(Projections.property("trackId"));
				criteria.add(Restrictions.in("trackId", trackIds));
				criteria.add(Restrictions.eq("trackStatus", TrackStatus.Status.UPLOADED));
				return criteria.list();
			}
		});
	}

	@Override
	public byte[] getChunk(String trackId, int chunkId) {
		byte[] bytes = chunkStorageService.get(trackId, chunkId);
		if (bytes.length > 0 && chunkId == TrackStatus.COMPLETE_UPLOAD) {
			updateDownloadsStats(trackId);
		}
		return bytes;
	}

	private void updateDownloadsStats(String trackId) {
		TrackUploadStatus status = ht.get(TrackUploadStatus.class, trackId);
		if (status != null) {
			status.setDownloadOn(new Date());
			status.setTotalDownloads((status.getTotalDownloads() + 1));
			ht.update(status);
		}
	}

	@Override
	public TrackStatus getTrackStatus(String trackId) {
		TrackUploadStatus status = ht.get(TrackUploadStatus.class, trackId);
		if (status == null) {
			TrackUploadStatus trackStatus = new TrackUploadStatus();
			trackStatus.setTrackId(trackId);
			trackStatus.setLastChunkNumber(0);
			trackStatus.setTrackStatus(Status.NOT_AVAILABLE);
			return trackStatus;
		}
		return status;
	}

	@Override
	public void cancelUpload(String trackId) {
		TrackUploadStatus status = ht.get(TrackUploadStatus.class, trackId);
		status.setTrackStatus(Status.INCOMPLETE);
		ht.update(status);
	}

	@Override
	public void storeMetadata(Track track) {
		ht.saveOrUpdate(track);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TrackSearchResult> findTracksByKeyword(final String keyword) {
		return ht.executeFind(new HibernateCallback<List<TrackSearchResult>>() {
			@Override
			public List<TrackSearchResult> doInHibernate(Session session) throws HibernateException, SQLException {

				FullTextSession fullTextSession = fullTextSessionfactory.createFullTextSession(session);
				Transaction transaction = fullTextSession.beginTransaction();

				QueryBuilder queryBuilder = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity(CachedTrack.class)
						.get();
				org.apache.lucene.search.Query query = queryBuilder.keyword().onFields("album", "artist", "name").matching(
						keyword).createQuery();

				FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(query, CachedTrack.class);
				fullTextQuery.setProjection(FullTextQuery.THIS, FullTextQuery.SCORE);
				fullTextQuery.setMaxResults(400);

				List<Object[]> resultList = fullTextQuery.list();

				transaction.commit();
				session.close();

				List<TrackSearchResult> trackSearchResultList = new ArrayList<TrackSearchResult>();
				for (Object[] result : resultList) {
					TrackSearchResult trackSearchResult = new TrackSearchResult((CachedTrack) result[0], (Float) result[1]);
					trackSearchResultList.add(trackSearchResult);
				}

				return trackSearchResultList;
			}
		});
	}

	class CreateLuceneIndexes implements HibernateCallback<Void> {
		@Override
		public Void doInHibernate(Session session) throws HibernateException, SQLException {
			long start = System.currentTimeMillis();
			LOG.info("Creating lucene indexes...");
			FullTextSession fullTextSession = fullTextSessionfactory.createFullTextSession(session);
			try {
				fullTextSession.createIndexer().startAndWait();
			} catch (InterruptedException e) {
				LOG.error("Unexpected exception creating lucene indexes", e);
				throw new HibernateException(e);
			}

			LOG.info("Lucene indexes creation took [ms]: " + (System.currentTimeMillis() - start));
			return null;
		}
	}

	class FullTextSessionfactory {
		public FullTextSession createFullTextSession(Session session) {
			return Search.getFullTextSession(session);
		}
	}
}
