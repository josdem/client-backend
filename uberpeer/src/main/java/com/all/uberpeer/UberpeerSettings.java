package com.all.uberpeer;

import org.springframework.stereotype.Component;

import com.all.peer.commons.util.PeerSettings;

@Component
public class UberpeerSettings extends PeerSettings {

	@Override
	public String getName() {
		return "ALL-UBERPEER[" + getPublicIp() + "]";
	}

}
