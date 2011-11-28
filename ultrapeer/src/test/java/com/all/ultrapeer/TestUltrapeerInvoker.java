package com.all.ultrapeer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.all.dht.DhtManager;
import com.all.ultrapeer.services.DefaultContactsService;
import com.all.ultrapeer.services.PresenceService;

@SuppressWarnings("deprecation")
// com.all.ultrapeer.services.DefaultContactsService has to die
public class TestUltrapeerInvoker {

	@InjectMocks
	private UltrapeerInvoker invoker = new UltrapeerInvoker();
	@Mock
	private DhtManager dhtManager;
	@Mock
	private PresenceService presenceService;
	@Mock
	private DefaultContactsService defaultContactsService;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldReturnTheDhtStoredKeys() throws Exception {
		Set<String> storedKeys = new HashSet<String>();
		String primaryKey = "A dht kuid";
		storedKeys.add(primaryKey);
		when(dhtManager.getAllKeys()).thenReturn(storedKeys);

		String result = (String) invoker.invoke("keys");

		assertTrue(result.contains(primaryKey));
	}

	@Test
	public void shouldReturnTheDhtLocalKeys() throws Exception {
		Map<String, String> storedKeys = new HashMap<String, String>();
		String primaryKey = "A dht kuid";
		String businessKey = "the business key";
		storedKeys.put(primaryKey, businessKey);
		when(dhtManager.getLocalKeys()).thenReturn(storedKeys);

		String result = (String) invoker.invoke("localKeys");

		assertTrue(result.contains(primaryKey));
		assertTrue(result.contains(businessKey));
	}

	@Test
	public void shouldReturnMoreInfoAboutAKuid() throws Exception {
		String expectedContent = "Expected content";
		String kuid = "kuid";
		when(dhtManager.more(kuid)).thenReturn(expectedContent);

		String result = (String) invoker.invoke("more." + kuid);

		assertEquals(expectedContent, result);
	}

	@Test
	public void shouldSupportVoidMethodsOrNullReturns() throws Exception {
		String result = (String) invoker.invoke("keys");
		assertNull(result);
	}

	@Test
	public void shouldReturnExceptionMessageIfSomethingFailed() throws Exception {
		String result = (String) invoker.invoke("someInvalidMethodName");

		assertTrue(result.contains("NoSuchMethodException"));
	}

	@Test
	public void shouldReturnDhtNodeId() throws Exception {
		String nodeId = "dht node id";
		when(dhtManager.getNodeId()).thenReturn(nodeId);

		assertEquals(nodeId, invoker.invoke("nodeId"));
	}

	@Test
	public void shouldGetPresenceInfo() throws Exception {
		invoker.invoke("presenceInfo");

		verify(presenceService).getInfo();
	}

	@Test
	public void shouldGetDefaultContacts() throws Exception {
		invoker.invoke("defaultContacts");

		verify(defaultContactsService).getInfo();
	}

	@Test
	public void shouldReloadDefaultContactsList() throws Exception {
		invoker.invoke("reloadDCL");
		verify(defaultContactsService).start();
		verify(defaultContactsService).getInfo();
	}

	@Test
	public void shouldExecuteGarbageCollection() throws Exception {
		long before = Runtime.getRuntime().freeMemory();
		invoker.invoke("gc");
		assertTrue(before <= Runtime.getRuntime().freeMemory());
	}

}
