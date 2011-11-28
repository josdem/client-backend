package com.all.uberpeer.services.feeds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.all.backend.commons.newsfeed.DataFeed;
import com.all.backend.commons.newsfeed.Feed;
import com.all.backend.commons.newsfeed.FeedSubscriber;
import com.all.backend.commons.newsfeed.FeedSubscriber.FeedStatus;
import com.all.shared.newsfeed.AllFeed;

@Service
public class AllFeed2DataFeedConverter {
	private final Log log = LogFactory.getLog(this.getClass());
	private Map<Class<?>, AllFeedSubscriberParser<?>> parsers = new HashMap<Class<?>, AllFeedSubscriberParser<?>>();

	@Autowired
	public void addFeedConverters(Collection<AllFeedSubscriberParser<?>> converters) {
		for (AllFeedSubscriberParser<?> converter : converters) {
			log.debug("ADDING PARSER FOR: " + converter.getClass());
			this.parsers.put(converter.getFeedClass(), converter);
		}
	}

	@SuppressWarnings("unchecked")
	public DataFeed convert(AllFeed feed) {
		log.debug("Parsing subscribers for feed: " + feed.getType());
		AllFeedSubscriberParser converter = parsers.get(feed.getClass());
		List<FeedSubscriber> subscribers = new ArrayList<FeedSubscriber>();
		subscribers.add(new FeedSubscriber(feed.getOwner().getId(), FeedStatus.DEFAULT));
		if (converter != null) {
			Collection extraSubscribers = converter.getExtraSubscribers(feed);
			if (extraSubscribers != null && !extraSubscribers.isEmpty()) {
				subscribers.addAll(extraSubscribers);
			}
		}
		log.debug("Parsing subscribers for feed COMPLETE: " + feed.getType() + " = " + subscribers.size());
		return new DataFeed(new Feed(feed), subscribers);
	}

}
