package com.all.ultrapeer.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import com.all.shared.json.JsonConverter;
import com.all.ultrapeer.UltrapeerConfig;

public abstract class BackendProxy {

	private final String serverKey;
	private final RestTemplate restTemplate;
	private final UltrapeerConfig ultrapeerConfig;

	public BackendProxy(RestTemplate restTemplate, UltrapeerConfig ultrapeerConfig, String serverKey) {
		this.restTemplate = restTemplate;
		this.ultrapeerConfig = ultrapeerConfig;
		this.serverKey = serverKey;
	}

	private String getUrl(String urlKey) {
		return ultrapeerConfig.getUrl(serverKey, urlKey);
	}

	public <T> T postForObject(String urlKey, Object request, Class<T> responseClass) {
		String response = restTemplate.postForObject(getUrl(urlKey),
				(request instanceof String) ? request : JsonConverter.toJson(request), String.class);
		return convertResponse(response, responseClass);
	}

	public <T extends Collection<V>, V> T postForCollection(String urlKey, Object body, Class<T> collectionType,
			Class<V> contentType) {
		String json = restTemplate.postForObject(getUrl(urlKey), JsonConverter.toJson(body), String.class);
		return JsonConverter.toTypedCollection(json, collectionType, contentType);
	}

	public void delete(String urlKey, Object... urlVars) {
		restTemplate.delete(getUrl(urlKey), urlVars);
	}

	public <T extends Collection<V>, V> T getForCollection(String urlKey, Class<T> collectionType, Class<V> contentType,
			Object... urlVars) {
		String json = restTemplate.getForObject(getUrl(urlKey), String.class, urlVars);
		return JsonConverter.toTypedCollection(json, collectionType, contentType);
	}

	public <T> void put(String urlKey, T request) {
		restTemplate.put(getUrl(urlKey), request instanceof String ? request : JsonConverter.toJson(request));
	}

	public <T> void put(String urlKey, T request, Object... urlVars) {
		restTemplate.put(getUrl(urlKey), request instanceof String ? request : JsonConverter.toJson(request), urlVars);
	}

	public <T> T getForObject(String urlKey, Class<T> responseClass, Object... urlVars) {
		String response = restTemplate.getForObject(getUrl(urlKey), String.class, urlVars);
		return convertResponse(response, responseClass);
	}

	@SuppressWarnings("unchecked")
	private <T> T convertResponse(String response, Class<T> responseClass) {
		if (response == null) {
			return null;
		}
		if (responseClass.equals(String.class)) {
			return (T) response;
		}
		return JsonConverter.toBean(response, responseClass);
	}

	public String send(String urlKey, HttpMethod method, final Object body, final Object[] urlVariables,
			final String[][] headers) {
		return restTemplate.execute(getUrl(urlKey), method, new RequestCallback() {
			@Override
			public void doWithRequest(ClientHttpRequest request) throws IOException {
				if (headers != null) {
					for (String[] header : headers) {
						request.getHeaders().add(header[0], header[1]);
					}
				}
				String bodyStr = null;
				if (body != null) {
					if (body instanceof String) {
						bodyStr = body.toString();
					} else {
						bodyStr = JsonConverter.toJson(body);
					}

				}
				if (bodyStr != null) {
					PrintWriter out = new PrintWriter(request.getBody());
					out.print(bodyStr);
					out.close();
				}
			}
		}, new ResponseExtractor<String>() {
			@Override
			public String extractData(ClientHttpResponse response) throws IOException {
				return null;
			}
		}, urlVariables);
	}

	public static String[] urlVars(String... vars) {
		return vars;
	}

	public static String[][] headers(String[]... headers) {
		return headers;
	}

	public static String[] header(String key, String value) {
		return new String[] { key, value };
	}

}
