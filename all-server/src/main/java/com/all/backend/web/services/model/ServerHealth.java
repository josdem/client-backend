package com.all.backend.web.services.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import net.sf.json.JSONObject;

import com.all.backend.web.services.model.BeanInfo.Status;
import com.all.shared.json.JsonConverter;
import com.all.shared.json.readers.JsonReader;

public class ServerHealth {

	private Map<String, BeanInfo> beans = new HashMap<String, BeanInfo>();
	private Map<String, UrlInfo> urls = new HashMap<String, UrlInfo>();

	public Collection<BeanInfo> getBeans() {
		return beans.values();
	}

	public Collection<UrlInfo> getUrls() {
		return urls.values();
	}

	public void initializing(Object bean, String name) {
		String id = BeanInfo.toId(bean, name);
		beans.put(id, new BeanInfo(id, bean, name));
		analizeUrls(bean);
	}

	public void initialized(Object bean, String name) {
		String id = BeanInfo.toId(bean, name);
		BeanInfo beanInfo = beans.get(id);
		if (beanInfo != null) {
			beanInfo.setStatus(Status.Ok);
		}
	}

	public void destroying(Object bean, String name) {
		String id = BeanInfo.toId(bean, name);
		BeanInfo beanInfo = beans.get(id);
		if (beanInfo != null) {
			beanInfo.setStatus(Status.Destroyed);
		}
	}

	private void analizeUrls(Object bean) {
		Class<?> clazz = bean.getClass();
		if (clazz.isAnnotationPresent(Controller.class)) {
			String[] basePaths = { "" };
			if (clazz.isAnnotationPresent(RequestMapping.class)) {
				RequestMapping mapping = clazz.getAnnotation(RequestMapping.class);
				basePaths = mapping.value();
			}
			Method[] methods = clazz.getDeclaredMethods();
			for (Method method : methods) {
				if (method.isAnnotationPresent(RequestMapping.class)) {
					RequestMapping mapping = method.getAnnotation(RequestMapping.class);
					String[] paths = mapping.value();
					RequestMethod[] requestMethods = mapping.method();
					for (String basePath : basePaths) {
						basePath = basePath.replaceAll("\\*\\*", "");
						for (String path : paths) {
							for (RequestMethod requestMethod : requestMethods) {
								String completePath = basePath + path;
								while (completePath.contains("//")) {
									completePath = completePath.replaceAll("//", "/");
								}
								String id = UrlInfo.toId(completePath, requestMethod);
								urls.put(id, new UrlInfo(requestMethod, completePath, bean, method.getName()));
							}
						}
					}
				}
			}
		}
	}

	static {
		JsonConverter.addJsonReader(ServerHealth.class, new JsonReader<ServerHealth>() {
			@SuppressWarnings("unchecked")
			@Override
			public ServerHealth read(String json) {
				ServerHealth health = new ServerHealth();
				JSONObject object = JSONObject.fromObject(json);
				String stringBeans = object.getJSONArray("beans").toString();
				ArrayList<BeanInfo> beans = JsonConverter.toTypedCollection(stringBeans, ArrayList.class, BeanInfo.class);
				for (BeanInfo beanInfo : beans) {
					health.beans.put(beanInfo.toId(beanInfo), beanInfo);
				}
				String stringUrls = object.getJSONArray("urls").toString();
				ArrayList<UrlInfo> urls = JsonConverter.toTypedCollection(stringUrls, ArrayList.class, UrlInfo.class);
				for (UrlInfo urlInfo : urls) {
					health.urls.put(UrlInfo.toId(urlInfo), urlInfo);
				}
				return health;
			}
		});
	}

}
