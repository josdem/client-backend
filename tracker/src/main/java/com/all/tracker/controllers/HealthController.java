package com.all.tracker.controllers;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class HealthController {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	@RequestMapping(method = GET, value = "/health")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public String getServerStatus() {
		log.info("\nACTION:HealthCheck");
		return HttpStatus.OK.name();
	}

}
