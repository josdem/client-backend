package com.all.ultrapeer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class TestUltrapeer {
	
	@Test
	public void shouldLoadSpringContextSuccesfully() throws Exception {
		String[] configLocations = new String[] { "/applicationContext.xml"};
		ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext(configLocations);
		assertTrue(applicationContext.isRunning());
		applicationContext.close();
		assertFalse(applicationContext.isRunning());
	}
}
