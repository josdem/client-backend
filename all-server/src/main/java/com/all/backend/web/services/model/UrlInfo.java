package com.all.backend.web.services.model;

import org.springframework.web.bind.annotation.RequestMethod;

public class UrlInfo {
	private RequestMethod method;
	private String path;
	private String handler;

	public UrlInfo() {
	}

	public UrlInfo(RequestMethod requestMethod, String path, Object bean, String method) {
		this.method = requestMethod;
		this.path = path;
		this.handler = bean.getClass().getName() + ":" + method;
	}

	public RequestMethod getMethod() {
		return method;
	}

	public void setMethod(RequestMethod method) {
		this.method = method;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public static String toId(UrlInfo urlInfo) {
		return urlInfo.path + "?" + urlInfo.method;
	}

	public static String toId(String completePath, RequestMethod requestMethod) {
		return completePath + "?" + requestMethod;
	}

}
