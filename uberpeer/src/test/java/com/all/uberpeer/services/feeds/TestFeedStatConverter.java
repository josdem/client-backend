package com.all.uberpeer.services.feeds;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.all.backend.commons.newsfeed.DataFeed;
import com.all.shared.model.ContactInfo;
import com.all.shared.newsfeed.RemoteLibraryBrowsingFeed;
import com.all.shared.stats.FeedStat;

public class TestFeedStatConverter {
	@InjectMocks
	private FeedStatConverter converter = new FeedStatConverter();
	@Mock
	private AllFeed2DataFeedConverter manager;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void shouldConvertAWrappedFeed() throws Exception {
		assertEquals(FeedStat.class, converter.getStatClass());
		ContactInfo a = new ContactInfo();
		ContactInfo b = new ContactInfo();
		RemoteLibraryBrowsingFeed feed = new RemoteLibraryBrowsingFeed(a, b);
		FeedStat feedStat = new FeedStat(feed);
		DataFeed convert = mock(DataFeed.class);
		when(manager.convert(any(RemoteLibraryBrowsingFeed.class))).thenReturn(convert);
		assertEquals(convert, converter.convert(feedStat));

	}
}
