package com.all.ultrapeer.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.all.dht.DhtManager;

public class TestSeedersCache {

	@InjectMocks
	private SeedersCache cache = new SeedersCache();
	@Mock
	private DhtManager dhtManager;
	@Captor
	private ArgumentCaptor<String> keysCaptor;
	@Captor
	private ArgumentCaptor<Collection<String>> valuesCaptor;

	@Before
	public void initialize() {
		initMocks(this);
		cache.initialize();
	}

	@After
	public void shutdown() {
		cache.shutdown();
	}

	@Test
	public void shouldAddAndRemoveSeedersAsynchronously() throws Exception {
		String seederA = "a@all.com";
		String seederB = "b@all.com";
		String hashcode1 = "hashcode1";
		String hashcode2 = "hashcode2";
		String hashcode3 = "hashcode3";

		List<String> expectedSeederAHashcodes = Arrays.asList(hashcode1, hashcode2);
		cache.addSeeder(seederA, expectedSeederAHashcodes);
		cache.addSeeder(seederB, Arrays.asList(hashcode2, hashcode3));

		int numHashcodes = 3;
		int repeatedHashcodes = 1;
		int numUpdates = numHashcodes + repeatedHashcodes; // 1 for each hashcode
		// reported by seeders
		// (hashcode2 is
		// reported twice)

		verify(dhtManager, timeout(1000).times(numUpdates)).save(keysCaptor.capture(), valuesCaptor.capture());
		List<String> keys = keysCaptor.getAllValues();
		List<Collection<String>> values = valuesCaptor.getAllValues();
		List<String> expectedSeedersForHashcode1 = Arrays.asList(seederA);
		List<String> expectedSeedersForHashcode2 = Arrays.asList(seederA, seederB);
		List<String> expectedSeedersForHashcode3 = Arrays.asList(seederB);

		assertEquals(hashcode1, keys.get(0));
		assertEquals(expectedSeedersForHashcode1, values.get(0));
		assertEquals(hashcode2, keys.get(1));
		assertEquals(Arrays.asList(seederA), values.get(1));
		assertEquals(hashcode2, keys.get(2));
		assertEquals(expectedSeedersForHashcode2, values.get(2));
		assertEquals(hashcode3, keys.get(3));
		assertEquals(expectedSeedersForHashcode3, values.get(3));

		assertTrue(cache.getHashcodes(seederA).containsAll(expectedSeederAHashcodes));
		assertTrue(cache.getHashcodes("seederC@all.com").isEmpty());

		cache.removeSeeder(seederA);
		verify(dhtManager, timeout(500)).delete(hashcode1);
		verify(dhtManager, timeout(500)).save(hashcode2, Arrays.asList(seederB));
		cache.removeSeeder(seederB);
		verify(dhtManager, timeout(500)).delete(hashcode2);
		verify(dhtManager, timeout(500)).delete(hashcode3);
	}

	@Test
	public void shouldAddMoreHashcodesForSameSeeder() throws Exception {
		String seeder = "a@all.com";
		String hashcode1 = "hashcode1";
		String hashcode2 = "hashcode2";

		List<String> firstList = Arrays.asList(hashcode1);
		cache.addSeeder(seeder, firstList);
		verify(dhtManager, timeout(500)).save(hashcode1, Arrays.asList(seeder));
		assertEquals(firstList, cache.getHashcodes(seeder));
		cache.addSeeder(seeder, Arrays.asList(hashcode2));
		verify(dhtManager, timeout(500)).save(hashcode2, Arrays.asList(seeder));
		assertTrue(cache.getHashcodes(seeder).containsAll(Arrays.asList(hashcode1, hashcode2)));
	}
	

	@SuppressWarnings("unchecked")
	@Test
	public void shouldGetSeedersForHashcode() throws Exception {
		String hashcode = "hashcode";
		String seederA = "seederA";
		String seederB = "seederB";
		String seederC = "seederC";
		Set<List<String>> allFoundSeeders = new HashSet<List<String>>();
		allFoundSeeders.add(new ArrayList<String>(Arrays.asList(seederA, seederB)));
		allFoundSeeders.add(new ArrayList<String>(Arrays.asList(seederC)));
		when(dhtManager.find(hashcode, ArrayList.class)).thenReturn((Set) allFoundSeeders);

		List<String> result = cache.getSeeders(hashcode);

		assertTrue(result.containsAll(Arrays.asList(seederA, seederB, seederC)));
	}

	@Test
	public void shouldNotReturnRepeatedHashcodesOrSeeders() throws Exception {
		String seeder = "a@all.com";
		String hashcode1 = "hashcode1";
		String hashcode2 = "hashcode2";

		List<String> hashcodes = Arrays.asList(hashcode1, hashcode2);
		cache.addSeeder(seeder, hashcodes);
		cache.addSeeder(seeder, hashcodes);
		cache.addSeeder(seeder, hashcodes);

		verify(dhtManager, timeout(500).times(6)).save(anyString(), valuesCaptor.capture());
		assertTrue(cache.getHashcodes(seeder).containsAll(hashcodes));
		assertEquals(hashcodes.size(), cache.getHashcodes(seeder).size());
		List<Collection<String>> allStoredSeeders = valuesCaptor.getAllValues();
		List<String> expectedSeederList = new ArrayList<String>();
		expectedSeederList.add(seeder);
		for (Collection<String> actualSeederList : allStoredSeeders) {
			assertEquals(expectedSeederList, actualSeederList);
		}
	}

}
