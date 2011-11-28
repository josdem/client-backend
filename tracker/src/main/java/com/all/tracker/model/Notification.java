package com.all.tracker.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Deprecated
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String header;
	private String description;
	private String link;
	private long date;
	
	public Notification(){}
	
	
	public Notification(String header, String description, String link, long date) {
		this.header = header;
		this.description = description;
		this.link = link;
		this.date = date;
	}



	public long getDate() {
		return date;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	public String getHeader() {
		return header;
	}

	public String getDescription() {
		return description;
	}

	public String getLink() {
		return link;
	}

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
}
