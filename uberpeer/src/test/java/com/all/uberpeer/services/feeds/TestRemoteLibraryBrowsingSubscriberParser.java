package com.all.uberpeer.services.feeds;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Test;

import com.all.backend.commons.newsfeed.FeedSubscriber;
import com.all.shared.model.ContactInfo;
import com.all.shared.newsfeed.RemoteLibraryBrowsingFeed;

public class TestRemoteLibraryBrowsingSubscriberParser {
	private RemoteLibraryBrowsingSubscriberParser parser = new RemoteLibraryBrowsingSubscriberParser();

	@SuppressWarnings("deprecation")
	@Test
	public void shouldConvert() throws Exception {
		ContactInfo visited = new ContactInfo();
		visited.setEmail("b");
		visited.setId(7L);
		ContactInfo owner = new ContactInfo();
		Collection<FeedSubscriber> extraSubscribers = parser.getExtraSubscribers(new RemoteLibraryBrowsingFeed(owner,
				visited));
		assertEquals(1, extraSubscribers.size());
		assertEquals(visited.getId(), extraSubscribers.iterator().next().getSubscriberId());
	}
}
