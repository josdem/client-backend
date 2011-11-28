package com.all.ultrapeer.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.logging.Log;
import org.limewire.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.all.dht.DhtManager;

@Component
public class SeedersCache {

	private final Log log = LogFactory.getLog(this.getClass());

	private final Map<String, Set<String>> seedersMap = new HashMap<String, Set<String>>();

	private final MultiValueMap hashcodesMap = MultiValueMap.decorate(new HashMap<String, Collection<String>>(),
			new HashSetFactory());

	private final BlockingQueue<Action> updatesQueue = new LinkedBlockingQueue<Action>();

	private final ExecutorService updatesExecutor = Executors.newSingleThreadExecutor();
	@Autowired
	private DhtManager dhtManager;

	@PostConstruct
	public void initialize() {
		updatesExecutor.execute(new SeedersCommandThread());
	}

	@PreDestroy
	public void shutdown() {
		updatesExecutor.shutdownNow();
	}

	public void addSeeder(String seederId, List<String> hashcodes) {
		updatesQueue.offer(new AddSeederAction(seederId, hashcodes));
	}

	public void removeSeeder(String seederId) {
		updatesQueue.offer(new RemoveSeederAction(seederId));
	}

	@SuppressWarnings("unchecked")
	public List<String> getSeeders(String hashcode) {
		Set<ArrayList> allSeedersLists = dhtManager.find(hashcode, ArrayList.class);
		List<String> seeders = new ArrayList<String>();
		for (List seedersList : allSeedersLists) {
			seeders.addAll(seedersList);
		}
		log.info("Search for " + hashcode + " throwed the following seeders: " + seeders);
		return seeders;
	}

	public List<String> getHashcodes(String seederId) {
		List<String> hashcodes = new ArrayList<String>();
		if (seedersMap.containsKey(seederId)) {
			hashcodes.addAll(seedersMap.get(seederId));
		}
		return hashcodes;
	}

	private abstract class Action {
		protected abstract void execute();
	}

	private final class RemoveSeederAction extends Action {

		private final String seederId;

		public RemoveSeederAction(String seederId) {
			this.seederId = seederId;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void execute() {
			Set<String> hashcodes = seedersMap.remove(seederId);
			if (hashcodes != null) {
				for (String hashcode : hashcodes) {
					Collection<String> seeders = hashcodesMap.getCollection(hashcode);
					seeders.remove(seederId);
					if (seeders.isEmpty()) {
						dhtManager.delete(hashcode);
					} else {
						dhtManager.save(hashcode, new ArrayList<String>(seeders));
					}
				}
				log.info(seederId + " was removed as seeder of " +  hashcodes.size() + " tracks.");
			}
		}

	}

	private final class AddSeederAction extends Action {

		private final String seederId;
		private final List<String> hashcodes;

		public AddSeederAction(String seederId, List<String> hashcodes) {
			this.seederId = seederId;
			this.hashcodes = hashcodes;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void execute() {
			Set<String> currentHashcodes = seedersMap.get(seederId);
			if (currentHashcodes == null) {
				seedersMap.put(seederId, new HashSet<String>(hashcodes));
			} else {
				currentHashcodes.addAll(hashcodes);
			}
			for (String hashcode : hashcodes) {
				hashcodesMap.put(hashcode, seederId);
				dhtManager.save(hashcode, new ArrayList<String>(hashcodesMap.getCollection(hashcode)));
			}
			log.info(seederId + " was added as seeder of " + hashcodes.size() + " tracks.");
		}

	}

	private final class SeedersCommandThread implements Runnable {
		@Override
		public void run() {
			while (true) {
				try {
					Action seedersCommand = updatesQueue.take();
					seedersCommand.execute();
				} catch (Exception e) {
					log.error("Unexpected error updating seeders.", e);
				}
			}

		}
	}

	private final class HashSetFactory implements Factory {
		@Override
		public Object create() {
			return new HashSet<String>();
		}
	}

}
