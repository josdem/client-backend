package com.all.tracker.service;

public class NoUpdateException extends Exception {

	private static final long serialVersionUID = 7473792811707985758L;
	
	public NoUpdateException(String message) {
		super(message);
	}
	
	public NoUpdateException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
