package com.all.rds.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.OneToMany;

@Entity
public class Category {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "categoryId")
	private Long id;
	private String name;
	@Lob
	private String description;
	private Date createdOn;
	private Date modifiedOn;
	@OneToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinTable(name = "CategoryTopPlaylist", joinColumns = { @JoinColumn(name = "categoryId") }, inverseJoinColumns = { @JoinColumn(name = "playlistId") })
	private List<TopPlaylist> topPlaylists = new ArrayList<TopPlaylist>();

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public Date getModifiedOn() {
		return modifiedOn;
	}

	public List<TopPlaylist> list() {
		return topPlaylists;
	}

}
