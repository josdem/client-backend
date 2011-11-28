package com.all.tracker.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class InstallerStatus {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String STATUS;
	private String MAC;
	private long timestamp;
	
	public InstallerStatus() {
		timestamp = System.currentTimeMillis();
	}
	
	public InstallerStatus(String status, String mac){
		setSTATUS(status);
		MAC = mac;
		timestamp = System.currentTimeMillis();
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public void setMAC(String mAC) {
		MAC = mAC;
	}

	public String getMAC() {
		return MAC;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setSTATUS(String sTATUS) {
		STATUS = sTATUS;
	}

	public String getSTATUS() {
		return STATUS;
	}
}
