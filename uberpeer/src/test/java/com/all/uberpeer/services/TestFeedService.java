package com.all.uberpeer.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.all.backend.commons.BackendConstants;
import com.all.backend.commons.newsfeed.DataFeed;
import com.all.backend.commons.newsfeed.Feed;
import com.all.backend.commons.newsfeed.FeedSubscriber;
import com.all.messengine.impl.StubMessEngine;
import com.all.shared.model.AllMessage;
import com.all.shared.stats.AllStat;
import com.all.uberpeer.persistence.FeedsDao;
import com.all.uberpeer.services.feeds.Stat2FeedConverter;

public class TestFeedService {
	@InjectMocks
	private FeedService service = new FeedService();
	@Spy
	private StubMessEngine stubEngine = new StubMessEngine();

	@Mock
	private FeedsDao dao;

	@Before
	public void setup() throws Exception {
		MockitoAnnotations.initMocks(this);
		stubEngine.setup(service);
	}

	@Test
	public void shouldNotBeAbleToConvertWithoutConvertersAndSaveNothing() throws Exception {
		List<AllStat> stats = new ArrayList<AllStat>();
		TestStat testStat = new TestStat();
		stats.add(testStat);
		stubEngine.send(new AllMessage<List<AllStat>>(BackendConstants.CONVERT_STATS_TO_FEED_TYPE, stats));
		verify(dao, never()).save(any(Feed.class));
		verify(dao, never()).save(any(FeedSubscriber.class));
	}

	@Test(timeout = 1000)
	public void shouldConvertStatsIntoFeedsAndStoreThem() throws Exception {

		TestConverter converter = new TestConverter();
		Collection<Stat2FeedConverter<?>> converters = new ArrayList<Stat2FeedConverter<?>>();
		converters.add(converter);

		Long feedId = 13L;
		when(dao.save(converter.feed)).thenReturn(feedId);

		service.addFeedConverters(converters);

		TestStat testStat = new TestStat();
		List<AllStat> stats = new ArrayList<AllStat>();
		stats.add(testStat);
		stubEngine.send(new AllMessage<List<AllStat>>(BackendConstants.CONVERT_STATS_TO_FEED_TYPE, stats));

		verify(dao).save(converter.feed);
		verify(dao).save(converter.subscriber1);
		verify(dao).save(converter.subscriber2);
		assertEquals(feedId, converter.subscriber1.getFeedId());
		assertEquals(feedId, converter.subscriber2.getFeedId());
	}

	class TestConverter implements Stat2FeedConverter<TestStat> {
		private Feed feed;
		private List<FeedSubscriber> subscriber;
		private DataFeed dataFeed;
		private FeedSubscriber subscriber1;
		private FeedSubscriber subscriber2;

		@SuppressWarnings("deprecation")
		public TestConverter() {
			feed = new Feed();
			subscriber1 = new FeedSubscriber();
			subscriber2 = new FeedSubscriber();
			List<FeedSubscriber> list = new ArrayList<FeedSubscriber>();
			list.add(subscriber2);
			list.add(subscriber1);
			this.subscriber = list;
			dataFeed = new DataFeed(feed, subscriber);
		}

		@Override
		public DataFeed convert(TestStat t) {
			return dataFeed;
		}

		@Override
		public Class<TestStat> getStatClass() {
			return TestStat.class;
		}

	}
}

@Ignore
class TestStat implements AllStat {
	@Override
	public Class<AllStat> getTypedClass() {
		return null;
	}

	@Override
	public Serializable getId() {
		return null;
	}

	@Override
	public long getTimestamp() {
		return 0;
	}

	@Override
	public void setTimestamp(long timestamp) {
	}

	@Override
	public Class<? extends AllStat> getStatType() {
		return null;
	}

	@Override
	public String getEmail() {
		return null;
	}
}
