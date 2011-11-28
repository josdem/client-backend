package com.all.uberpeer.services.feeds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import com.all.backend.commons.newsfeed.FeedSubscriber;
import com.all.backend.commons.newsfeed.FeedSubscriber.FeedStatus;
import com.all.shared.newsfeed.RemoteLibraryBrowsingFeed;

@Component
public class RemoteLibraryBrowsingSubscriberParser implements AllFeedSubscriberParser<RemoteLibraryBrowsingFeed> {

	@Override
	public Class<RemoteLibraryBrowsingFeed> getFeedClass() {
		return RemoteLibraryBrowsingFeed.class;
	}

	@Override
	public Collection<FeedSubscriber> getExtraSubscribers(RemoteLibraryBrowsingFeed feed) {
		List<FeedSubscriber> subscribers = new ArrayList<FeedSubscriber>();
		subscribers.add(new FeedSubscriber(feed.getVisited().getId(), FeedStatus.DEFAULT));
		return subscribers;
	}
}
