package com.all.ultrapeer.beans;

import com.all.shared.model.User;
import com.all.shared.stats.UserSpecs;

public class UserProfile {

	private final User user;

	private final UserSpecs userSpecs;

	public UserProfile(User user, UserSpecs userSpecs) {
		this.user = user;
		this.userSpecs = userSpecs;
	}

	public User getUser() {
		return user;
	}

	public UserSpecs getUserSpecs() {
		return userSpecs;
	}

}
