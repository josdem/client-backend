package com.all.tracker.controllers;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.springframework.http.HttpStatus;

public class TestHealthController {
	
	@InjectMocks
	private HealthController controller = new HealthController();
	
	@Test
	public void shouldRespondHealthStatus() throws Exception {
		assertEquals(HttpStatus.OK.name(), controller.getServerStatus());
	}

}
