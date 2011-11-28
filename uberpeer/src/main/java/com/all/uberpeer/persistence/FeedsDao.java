package com.all.uberpeer.persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;

import com.all.backend.commons.newsfeed.Feed;
import com.all.backend.commons.newsfeed.FeedSubscriber;

@Repository
public class FeedsDao {

	@Autowired
	private HibernateTemplate ht;

	public Long save(Feed feed) {
		return (Long) ht.save(feed);
	}

	public void save(FeedSubscriber subscriber) {
		ht.save(subscriber);
	}

}
