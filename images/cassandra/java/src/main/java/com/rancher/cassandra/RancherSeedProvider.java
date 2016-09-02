package com.rancher.cassandra;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.cassandra.config.Config;
import org.apache.cassandra.config.ConfigurationLoader;
import org.apache.cassandra.config.YamlConfigurationLoader;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.cassandra.locator.SeedProvider;
import org.apache.cassandra.locator.SimpleSeedProvider;
import org.apache.cassandra.utils.FBUtilities;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RancherSeedProvider implements SeedProvider {

	private static final Logger logger = LoggerFactory.getLogger(RancherSeedProvider.class);
	
	private static final String RANCHER_METADATA = "http://rancher-metadata.rancher.internal/2015-12-19";
	
	private final List<InetAddress> defaultSeeds;
	
	/**
	 * Create default seeds
	 * @param params
	 */
	public RancherSeedProvider(Map<String, String> params) {
		defaultSeeds = createDefaultSeeds();
	}
	
	public List<InetAddress> getSeeds() {
		logger.info("Getting seeds from Rancher metadata");
		
		List<InetAddress> seeds = new ArrayList<InetAddress>();
		try {
			URL url = new URL(String.format("%s/self/service/containers", RANCHER_METADATA));
			HttpURLConnection cxn = (HttpURLConnection) url.openConnection();
			cxn.addRequestProperty("Accept", "application/json");
			
            ObjectMapper mapper = new ObjectMapper();
            Container[] containers = mapper.readValue(cxn.getInputStream(), Container[].class);
            for (Container container : containers) {
            	logger.info("Found seed: " + container.primary_ip);
            	seeds.add(InetAddress.getByName(container.primary_ip));
            }

		} catch (Exception e) {
			logger.warn("Failed to communicate with Rancher metadata service, using default seeds", e);
			return defaultSeeds;
		}
		
		return Collections.unmodifiableList(seeds);
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
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Service {
    	public List<Container> containers;
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Container {
    	public String primary_ip;
    }
}
