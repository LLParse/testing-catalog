package com.rancher.cassandra;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.cassandra.config.Config;
import org.apache.cassandra.config.ConfigurationLoader;
import org.apache.cassandra.config.YamlConfigurationLoader;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.locator.SeedProvider;
import org.apache.cassandra.locator.SimpleSeedProvider;
import org.apache.cassandra.utils.FBUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RancherSeedProvider implements SeedProvider {

	private static final Logger logger = LoggerFactory.getLogger(RancherSeedProvider.class);
	
	private static final String RANCHER_METADATA = "http://rancher-metadata.rancher.internal/2015-12-19";
	
	private final List<InetAddress> defaultSeeds;
	
	public RancherSeedProvider() {
		defaultSeeds = createDefaultSeeds();
	}
	
	public List<InetAddress> getSeeds() {
		logger.info("Getting seeds from Rancher metadata");
		
		// TODO (llparse) Iterate through service containers
		
		return Collections.unmodifiableList(defaultSeeds);
	}

    /**
     * Code taken from {@link SimpleSeedProvider}
     * @return
     */
    protected List<InetAddress> createDefaultSeeds() {
        Config conf;
        try {
            conf = loadConfig();
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
        String[] hosts = conf.seed_provider.parameters.get("seeds").split(",", -1);
        List<InetAddress> seeds = new ArrayList<InetAddress>();
        for (String host : hosts) {
            try {
                seeds.add(InetAddress.getByName(host.trim()));
            }
            catch (UnknownHostException ex) {
                // not fatal... DD will bark if there end up being zero seeds.
                logger.warn("Seed provider couldn't lookup host {}", host);
            }
        }

        if(seeds.size() == 0) {
            try {
                seeds.add(InetAddress.getLocalHost());
            } catch (UnknownHostException e) {
                logger.warn("Seed provider couldn't lookup localhost");
            }
        }
        return Collections.unmodifiableList(seeds);
    }

    /**
     * Code taken from {@link SimpleSeedProvider}
     * @return
     */
    protected static Config loadConfig() throws ConfigurationException {
        String loaderClass = System.getProperty("cassandra.config.loader");
        ConfigurationLoader loader = loaderClass == null
                ? new YamlConfigurationLoader()
                : FBUtilities.<ConfigurationLoader>construct(loaderClass, "configuration loading");
        return loader.loadConfig();
    }
}
