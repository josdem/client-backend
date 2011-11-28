package com.all.uberpeer.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.all.backend.commons.BackendConstants;
import com.all.backend.commons.newsfeed.DataFeed;
import com.all.backend.commons.newsfeed.FeedSubscriber;
import com.all.messengine.MessageMethod;
import com.all.shared.stats.AllStat;
import com.all.uberpeer.persistence.FeedsDao;
import com.all.uberpeer.services.feeds.Stat2FeedConverter;

@Service
public class FeedService {
	private final Log log = LogFactory.getLog(this.getClass());

	@Autowired
	private FeedsDao statsDao;

	private Map<Class<?>, Stat2FeedConverter<? extends AllStat>> map = new HashMap<Class<?>, Stat2FeedConverter<? extends AllStat>>();

	@MessageMethod(BackendConstants.CONVERT_STATS_TO_FEED_TYPE)
	public void createFeedsFromStats(Collection<AllStat> stats) {
		log.info("Converting stats...");
		for (AllStat stat : stats) {
			try {
				DataFeed dataFeed = convert(stat);
				if (dataFeed == null) {
					continue;
				}

				Long feedId = statsDao.save(dataFeed.getFeed());

				for (FeedSubscriber feedSubscriber : dataFeed.getSubscribers()) {
					feedSubscriber.setFeedId(feedId);
					statsDao.save(feedSubscriber);
				}
			} catch (Exception e) {
				log.error("Unexpected error saving stat.", e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private DataFeed convert(AllStat stat) {
		Stat2FeedConverter converter = map.get(stat.getClass());
		if (converter != null) {
			DataFeed convert = converter.convert(stat);
			log.debug("stat: " + stat.getClass() + " converted " + convert.getFeed().getType() + " s:"
					+ convert.getSubscribers().size());
			return convert;
		}
		log.debug("No converter found for stat: " + stat.getClass());
		return null;
	}

	@Autowired
	public void addFeedConverters(Collection<Stat2FeedConverter<?>> converters) {
		for (Stat2FeedConverter<?> converter : converters) {
			log.debug("ADDING CONVERTER FOR: " + converter.getClass());
			map.put(converter.getStatClass(), converter);
		}
	}

}
