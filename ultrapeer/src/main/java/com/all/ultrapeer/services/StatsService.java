package com.all.ultrapeer.services;

import static com.all.shared.messages.MessEngineConstants.USAGE_STATS_TYPE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.all.messengine.MessageMethod;
import com.all.shared.stats.AllStat;
import com.all.ultrapeer.util.AllServerProxy;

@Service
public class StatsService {
	private final Log log = LogFactory.getLog(this.getClass());

	@Autowired
	private AllServerProxy allServerProxy;

	private List<AllStat> cachedStats = new LinkedList<AllStat>();

	@MessageMethod(USAGE_STATS_TYPE)
	public void addStats(Collection<? extends AllStat> stats) {
		List<AllStat> statsToSend = new ArrayList<AllStat>(stats);
		synchronized (cachedStats) {
			statsToSend.addAll(cachedStats);
			cachedStats.clear();
		}
		if (!stats.isEmpty()) {
			try {
				allServerProxy.put("stats.put", statsToSend);
				synchronized (cachedStats) {
					cachedStats.clear();
				}
			} catch (Exception e) {
				log.warn("Unexpected exception sending stats.", e);
				cachedStats.addAll(statsToSend);
			}
		}
	}
}
