package com.all.ultrapeer.util;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.all.shared.stats.AllStat;
import com.all.ultrapeer.UltrapeerConfig;

@Service
public class AllServerProxy extends BackendProxy {

	public static final String ALL_SERVER_KEY = "all_server_uri";

	@Autowired
	public AllServerProxy(RestTemplate restTemplate, UltrapeerConfig ultrapeerConfig) {
		super(restTemplate, ultrapeerConfig, ALL_SERVER_KEY);
	}

	public void putStats(Collection<AllStat> body) {
		// TODO Auto-generated method stub

	}

}
