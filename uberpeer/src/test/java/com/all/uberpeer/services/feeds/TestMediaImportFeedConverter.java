package com.all.uberpeer.services.feeds;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.all.backend.commons.newsfeed.DataFeed;
import com.all.backend.commons.newsfeed.Feed;
import com.all.backend.commons.newsfeed.FeedSubscriber;
import com.all.backend.commons.newsfeed.FeedSubscriber.FeedStatus;
import com.all.shared.json.JsonConverter;
import com.all.shared.model.ContactInfo;
import com.all.shared.newsfeed.AllFeed;
import com.all.shared.newsfeed.FeedType;
import com.all.shared.newsfeed.MediaImportFeed;
import com.all.shared.stats.MediaImportStat;
import com.all.shared.stats.MediaImportStat.ImportType;
import com.all.uberpeer.persistence.ContactDao;

public class TestMediaImportFeedConverter {

	@InjectMocks
	private MediaImportFeedConverter converter = new MediaImportFeedConverter();
	@Mock
	private ContactDao contactDao;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldCreateDataFeedFromMediaImportStat() throws Exception {
		String email = "user@all.com";
		MediaImportStat stat = new MediaImportStat(email, ImportType.ITUNES, 10, 5, 1);
		@SuppressWarnings("deprecation")
		ContactInfo contact = new ContactInfo();
		contact.setNickName("some name");
		contact.setEmail(email);
		contact.setId(7L);
		when(contactDao.findContactByEmail(email)).thenReturn(contact);
		DataFeed dataFeed = converter.convert(stat);
		assertNotNull(dataFeed);
		Feed feed = dataFeed.getFeed();
		assertEquals(Integer.valueOf(FeedType.MEDIA_IMPORT), feed.getType());
		assertNotNull(feed.getTimestamp());
		String serializedFeed = feed.getBody();
		AllFeed actualFeed = JsonConverter.toBean(serializedFeed, AllFeed.class);
		assertNotNull(actualFeed);
		ContactInfo owner = actualFeed.getOwner();
		assertEquals(contact.getNickName(), owner.getNickName());
		assertEquals(FeedType.MEDIA_IMPORT, actualFeed.getType());
		assertTrue(((MediaImportFeed) actualFeed).isFromItunes());
		assertEquals(stat.getTotalTracks(), ((MediaImportFeed) actualFeed).getTotalTracks());
		assertEquals(stat.getTotalPlaylists(), ((MediaImportFeed) actualFeed).getTotalPlaylists());
		assertEquals(stat.getTotalFolders(), ((MediaImportFeed) actualFeed).getTotalFolders());
		List<FeedSubscriber> subscribers = dataFeed.getSubscribers();
		assertNotNull(subscribers);
		assertEquals(1, subscribers.size());
		FeedSubscriber feedSubscriber = subscribers.get(0);
		assertEquals(contact.getId(), feedSubscriber.getSubscriberId());
		assertEquals(FeedStatus.DEFAULT, feedSubscriber.getStatus());
	}

	@Test
	public void shouldReturnClassTypeAccordingly() throws Exception {
		assertEquals(MediaImportStat.class, converter.getStatClass());
	}
}
