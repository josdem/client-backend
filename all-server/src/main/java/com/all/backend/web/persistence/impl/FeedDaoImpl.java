package com.all.backend.web.persistence.impl;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.validation.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;

import com.all.backend.commons.newsfeed.Feed;
import com.all.backend.web.persistence.FeedDao;

@Repository("feedDao")
public class FeedDaoImpl extends BaseDaoImpl implements FeedDao {
	private static final String FEED_PAGINATION_KEY = "feedPagination";

	private static final String QUERY_MAX_RESULTS = "feedMaxResults";

	private Log log = LogFactory.getLog(FeedDaoImpl.class);

	@Autowired
	private Properties config;
	private int maxResults;
	private int feedsPagination;

	@Autowired
	public FeedDaoImpl(HibernateTemplate ht, Validator validator, SimpleJdbcTemplate jdbcTemplate) {
		super(ht, validator, jdbcTemplate);
	}

	@PostConstruct
	public void setup() {
		maxResults = Integer.parseInt(config.getProperty(QUERY_MAX_RESULTS));
		feedsPagination = Integer.parseInt(config.getProperty(FEED_PAGINATION_KEY));
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Feed> getFeeds(final List<Long> userRelationships, final Date date) {

		List<Feed> result = ht.execute(new HibernateCallback<List<Feed>>() {
			@Override
			public List<Feed> doInHibernate(Session session) throws HibernateException, SQLException {
				Query query = session
						.createQuery("select distinct(fd) from Feed fd, FeedSubscriber fs where fd.timestamp>:date and fs.subscriberId in(:userRelationships) and fd.id = fs.feedId order by fd.timestamp desc");
				log.info("date from query: " + date);
				query.setParameter("date", date);
				query.setParameterList("userRelationships", userRelationships);
				return (List<Feed>) query.list();
			}
		});
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Feed> getFeeds(final List<Long> userRelationships) {
		List<Feed> result = ht.execute(new HibernateCallback<List<Feed>>() {
			@Override
			public List<Feed> doInHibernate(Session session) throws HibernateException, SQLException {
				log.info("setting maxResults to: " + maxResults);
				Query query = session
						.createQuery("select distinct(fd) from Feed fd, FeedSubscriber fs where fs.subscriberId in(:userRelationships) and fd.id = fs.feedId order by fd.timestamp desc");
				query.setParameterList("userRelationships", userRelationships);
				query.setMaxResults(maxResults);
				return (List<Feed>) query.list();
			}
		});
		return result;
	}

	@Override
	public <T> List<T> find(Class<T> clazz, Set<? extends Serializable> ids) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Feed> getOldFeeds(final List<Long> userRelationships, final Date date) {
		List<Feed> result = ht.execute(new HibernateCallback<List<Feed>>() {
			@Override
			public List<Feed> doInHibernate(Session session) throws HibernateException, SQLException {
				Query query = session
						.createQuery("select distinct(fd) from Feed fd, FeedSubscriber fs where fd.timestamp<:date and fs.subscriberId in(:userRelationships) and fd.id = fs.feedId order by fd.timestamp desc");
				log.info("date from query: " + date);
				query.setParameter("date", date);
				query.setParameterList("userRelationships", userRelationships);
				query.setMaxResults(feedsPagination);
				return (List<Feed>) query.list();
			}
		});
		return result;
	}

}
