package com.all.tracker.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class DownloaderMetrics {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String IP;
	private String DATEREGISTERED;
	private String DOWNLOADVERSION;
	private String CODE;
	private String BYTES;
	private String EXPLORER;
	private long timestamp;
	
	public DownloaderMetrics() {
		timestamp = System.currentTimeMillis();
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getIP() {
		return IP;
	}
	public void setIP(String iP) {
		IP = iP;
	}
	public String getDATEREGISTERED() {
		return DATEREGISTERED;
	}
	public void setDATEREGISTERED(String dATEREGISTERED) {
		DATEREGISTERED = dATEREGISTERED;
	}
	public String getDOWNLOADVERSION() {
		return DOWNLOADVERSION;
	}
	public void setDOWNLOADVERSION(String dOWNLOADVERSION) {
		DOWNLOADVERSION = dOWNLOADVERSION;
	}
	public String getCODE() {
		return CODE;
	}
	public void setCODE(String cODE) {
		CODE = cODE;
	}
	public String getBYTES() {
		return BYTES;
	}
	public void setBYTES(String bYTES) {
		BYTES = bYTES;
	}
	public String getEXPLORER() {
		return EXPLORER;
	}
	public void setEXPLORER(String eXPLORER) {
		EXPLORER = eXPLORER;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
