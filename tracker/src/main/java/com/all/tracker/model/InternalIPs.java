package com.all.tracker.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class InternalIPs {

	@Id
	private String ip;
	private long timestamp;
	
	public InternalIPs() {
		timestamp = System.currentTimeMillis();
	}
	
	public InternalIPs(String ip){
		this.setIp(ip);
		timestamp = System.currentTimeMillis();
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getIp() {
		return ip;
	}
}
