package com.all.uberpeer.services.feeds;

import java.util.Collection;

import com.all.backend.commons.newsfeed.FeedSubscriber;
import com.all.shared.newsfeed.AllFeed;

public interface AllFeedSubscriberParser<T extends AllFeed> {
	Class<T> getFeedClass();

	Collection<? extends FeedSubscriber> getExtraSubscribers(T feed);
}
