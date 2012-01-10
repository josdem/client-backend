package com.all.backend.web.services.model;

import com.all.shared.messages.FriendshipRequestStatus;
import com.all.shared.model.ContactRequest;

public class FriendshipRequestResult {
	private final FriendshipRequestStatus status;
	private final ContactRequest request;

	public FriendshipRequestResult(FriendshipRequestStatus status, ContactRequest request) {
		this.status = status;
		this.request = request;
	}

	public FriendshipRequestStatus getStatus() {
		return status;
	}

	public ContactRequest getRequest() {
		return request;
	}

}
