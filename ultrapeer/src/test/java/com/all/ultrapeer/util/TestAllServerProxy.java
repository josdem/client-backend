package com.all.ultrapeer.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import com.all.shared.json.JsonConverter;
import com.all.shared.model.User;
import com.all.ultrapeer.UltrapeerConfig;

public class TestAllServerProxy  {

	private AllServerProxy userServerProxy;
	@Mock
	private RestTemplate restTemplate;
	@Mock
	private UltrapeerConfig ultrapeerConfig;

	private String urlKey = "urlKey";

	private Object urlVar = "someVar";

	private String actualUrl = "actualUrl";

	private User user = new User();

	String jsonUser;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		userServerProxy = new AllServerProxy(restTemplate, ultrapeerConfig);
		when(ultrapeerConfig.getUrl(AllServerProxy.ALL_SERVER_KEY, urlKey)).thenReturn(actualUrl);
		user.setEmail("user@all.com");
		jsonUser = JsonConverter.toJson(user);
	}

	@Test
	public void shouldGetForBean() throws Exception {
		when(restTemplate.getForObject(actualUrl, String.class, urlVar)).thenReturn(JsonConverter.toJson(user));

		User result = userServerProxy.getForObject(urlKey, User.class, urlVar);

		verify(ultrapeerConfig).getUrl(AllServerProxy.ALL_SERVER_KEY, urlKey);
		assertEquals(user.getEmail(), result.getEmail());
	}

	@Test
	public void shouldGetForString() throws Exception {
		String expected = "expected";
		when(restTemplate.getForObject(actualUrl, String.class, urlVar)).thenReturn(expected);
		String actual = userServerProxy.getForObject(urlKey, String.class, urlVar);

		assertEquals(expected, actual);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldGetForCollection() throws Exception {
		ArrayList<User> list = new ArrayList<User>();
		list.add(user);
		String jsonList = JsonConverter.toJson(list);
		when(restTemplate.getForObject(actualUrl, String.class, urlVar)).thenReturn(jsonList);

		ArrayList<User> users = userServerProxy.getForCollection(urlKey, ArrayList.class, User.class, urlVar);
		assertNotNull(users);
		assertEquals(list.size(), users.size());
		assertEquals(list.get(0).getEmail(), users.get(0).getEmail());
	}

	@Test
	public void shouldPostForBean() throws Exception {
		when(restTemplate.postForObject(eq(actualUrl), eq(jsonUser), eq(String.class))).thenReturn(jsonUser);

		User actual = userServerProxy.postForObject(urlKey, user, User.class);

		assertNotNull(actual);
		assertEquals(user.getEmail(), actual.getEmail());

	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldPostForCollection() throws Exception {
		List<User> expected = new ArrayList<User>();
		expected.add(user);
		when(restTemplate.postForObject(eq(actualUrl), eq(jsonUser), eq(String.class))).thenReturn(JsonConverter.toJson(expected));

		List<User> actual = userServerProxy.postForCollection(urlKey, user, ArrayList.class, User.class);

		assertNotNull(actual);
		assertEquals(expected.size(), actual.size());
	}

	@Test
	public void shouldPostForString() throws Exception {
		String someString = "some text";
		String expected = "expected";
		when(restTemplate.postForObject(eq(actualUrl), eq(someString), eq(String.class))).thenReturn(expected);

		String actual = userServerProxy.postForObject(urlKey, someString, String.class);

		assertNotNull(actual);
		assertEquals(expected, actual);
	}

	@Test
	public void shouldPutABean() throws Exception {
		userServerProxy.put(urlKey, user);

		verify(restTemplate).put(actualUrl, jsonUser);
	}

	@Test
	public void shouldPutAString() throws Exception {
		String request = "someString";
		userServerProxy.put(urlKey, request);

		verify(restTemplate).put(actualUrl, request);
	}

	@Test
	public void shouldPutABeanUsingUrlVars() throws Exception {
		userServerProxy.put(urlKey, user, urlVar);

		verify(restTemplate).put(actualUrl, jsonUser, urlVar);

	}

	@Test
	public void shouldPutAStringUsingUrlVars() throws Exception {
		String request = "someString";
		userServerProxy.put(urlKey, request, urlVar);

		verify(restTemplate).put(actualUrl, request, urlVar);
	}

	@Test
	public void shouldDelete() throws Exception {
		userServerProxy.delete(urlKey, urlVar);

		verify(restTemplate).delete(actualUrl, urlVar);
	}

	@Test
	public void shouldGetNullValue() throws Exception {
		when(restTemplate.getForObject(actualUrl, String.class)).thenReturn(null);

		User user = userServerProxy.getForObject(urlKey, User.class);
		assertNull(user);
	}

}
