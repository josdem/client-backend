package com.all.uberpeer.services.feeds;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import com.all.backend.commons.newsfeed.DataFeed;
import com.all.backend.commons.newsfeed.FeedSubscriber;
import com.all.backend.commons.newsfeed.FeedSubscriber.FeedStatus;
import com.all.shared.model.ContactInfo;
import com.all.shared.newsfeed.RemoteLibraryBrowsingFeed;

public class TestAllFeed2DataFeedConverter {
	private AllFeed2DataFeedConverter converter = new AllFeed2DataFeedConverter();

	@Test
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void shouldConvertUsingParser() throws Exception {
		// in reality should add the subscribers...
		AllFeedSubscriberParser parser = mock(AllFeedSubscriberParser.class);
		when(parser.getFeedClass()).thenReturn(RemoteLibraryBrowsingFeed.class);
		ArrayList<FeedSubscriber> extraSubscribers = new ArrayList<FeedSubscriber>();
		extraSubscribers.add(new FeedSubscriber(2L, FeedStatus.DEFAULT));
		extraSubscribers.add(new FeedSubscriber(3L, FeedStatus.DEFAULT));
		when(parser.getExtraSubscribers(any(RemoteLibraryBrowsingFeed.class))).thenReturn(extraSubscribers);

		Collection<AllFeedSubscriberParser<?>> parsers = new ArrayList<AllFeedSubscriberParser<?>>();
		parsers.add(parser);

		converter.addFeedConverters(parsers);
		ContactInfo a = new ContactInfo();
		a.setEmail("a");
		a.setId(1L);
		ContactInfo b = new ContactInfo();
		b.setEmail("b");
		DataFeed convert = converter.convert(new RemoteLibraryBrowsingFeed(a, b));
		assertEquals(3, convert.getSubscribers().size());
		assertEquals(Long.valueOf(1L), convert.getSubscribers().get(0).getSubscriberId());
		assertEquals(Long.valueOf(2L), convert.getSubscribers().get(1).getSubscriberId());
		assertEquals(Long.valueOf(3L), convert.getSubscribers().get(2).getSubscriberId());
	}

	@Test
	@SuppressWarnings({ "unchecked", "deprecation" })
	public void shouldNotConvertIfParserNotSet() throws Exception {
		ContactInfo a = new ContactInfo();
		a.setEmail("a");
		a.setId(2L);
		ContactInfo b = new ContactInfo();
		b.setEmail("b");
		DataFeed convert = converter.convert(new RemoteLibraryBrowsingFeed(a, b));
		assertEquals(1, convert.getSubscribers().size());
		assertEquals(a.getId(), convert.getSubscribers().get(0).getSubscriberId());

		AllFeedSubscriberParser parser = mock(AllFeedSubscriberParser.class);
		when(parser.getFeedClass()).thenReturn(RemoteLibraryBrowsingFeed.class);
		when(parser.getExtraSubscribers(any(RemoteLibraryBrowsingFeed.class))).thenReturn(null);

		Collection<AllFeedSubscriberParser<?>> parsers = new ArrayList<AllFeedSubscriberParser<?>>();
		parsers.add(parser);

		converter.addFeedConverters(parsers);

		convert = converter.convert(new RemoteLibraryBrowsingFeed(a, b));
		assertEquals(1, convert.getSubscribers().size());
		assertEquals(a.getId(), convert.getSubscribers().get(0).getSubscriberId());
	}
}
