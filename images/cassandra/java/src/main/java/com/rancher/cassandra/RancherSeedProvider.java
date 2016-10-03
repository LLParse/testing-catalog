package com.rancher.cassandra;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
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
	private final int numSeeds;
	
	/**
	 * Create default seeds
	 * @param params
	 */
	public RancherSeedProvider(Map<String, String> params) {
		defaultSeeds = createDefaultSeeds();
		
		String numSeedsVar = getEnvOrDefault("CASSANDRA_NUM_SEEDS", "3");
		numSeeds = Integer.parseInt(numSeedsVar);
	}
	
	public List<InetAddress> getSeeds() {
		logger.info(String.format("Getting %d seeds max from Rancher metadata", numSeeds));
		
		List<InetAddress> seeds = new ArrayList<InetAddress>();
		try {
			URL url = new URL(String.format("%s/self/service/containers", RANCHER_METADATA));
			HttpURLConnection cxn = (HttpURLConnection) url.openConnection();
			cxn.addRequestProperty("Accept", "application/json");
			
            ObjectMapper mapper = new ObjectMapper();
            Container[] containerArray = mapper.readValue(cxn.getInputStream(), Container[].class);

        	// sort the containers for best-effort static seed selection
            List<Container> containers = Arrays.asList(containerArray);
            Collections.sort(containers);
            
            for (Container container : containers) {
            	logger.info(String.format("Found seed: %s (%d)", container.primary_ip, container.service_index));
            	seeds.add(InetAddress.getByName(container.primary_ip));
            	if (seeds.size() >= numSeeds) {
            		break;
            	}
            }

		} catch (Exception e) {
			logger.warn("Failed to communicate with Rancher metadata service, using default seeds", e);
			return defaultSeeds;
		}
		
		return Collections.unmodifiableList(seeds);
	}
	
	/**
	 * Code taken from https://github.com/kubernetes/kubernetes
	 * @param var
	 * @param def
	 * @return
	 */
    private static String getEnvOrDefault(String var, String def) {
        String val = System.getenv(var);
        if (val == null) {
            val = def;
        }
        return val;
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
    static class Container implements Comparable<Container> {
    	public String primary_ip;
    	public Integer service_index;
    	
		public int compareTo(Container o) {
			return service_index - o.service_index;
		}
    }
}
