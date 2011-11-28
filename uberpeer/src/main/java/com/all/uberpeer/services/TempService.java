package com.all.uberpeer.services;

import org.apache.commons.logging.Log;
import org.limewire.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.all.messengine.MessageMethod;

@Service
public class TempService {

	private final Log log = LogFactory.getLog(this.getClass());

	@MessageMethod("testRmiType")
	public void testRmiMessageProcessing(String parameter) {
		log.info("Processing " + parameter + " from RMI invocation.");
	}
}
