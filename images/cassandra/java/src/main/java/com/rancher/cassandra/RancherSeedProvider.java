package com.rancher.cassandra;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.cassandra.locator.SeedProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RancherSeedProvider implements SeedProvider {

	private static final Logger logger = LoggerFactory.getLogger(RancherSeedProvider.class);
	
	// private static String RANCHER_METADATA = "http://rancher-metadata.rancher.internal/2015-12-19";
	
	public List<InetAddress> getSeeds() {
		logger.info("Getting seeds from Rancher metadata");
		// TODO (llparse) Iterate through service containers
		return new ArrayList<InetAddress>();
	}

}
