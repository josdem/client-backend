package com.all.backend.web.services.model;

public class BeanInfo {
	public enum Status {
		Initializing, Ok, Destroyed
	}

	private String name;
	private String className;
	private Status status = Status.Initializing;

	public BeanInfo(String id, Object bean, String name) {
		this.className = bean.getClass().getName();
		this.name = name;

	}

	public BeanInfo() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		if (status != null) {
			this.status = status;
		}
	}

	public static String toId(Object bean, String name) {
		return bean.getClass().getName() + "|" + name;
	}

	public String toId(BeanInfo beanInfo) {
		return beanInfo.className + "|" + beanInfo.name;
	}

}
