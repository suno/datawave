package datawave.webservice.operations.user;

import org.apache.accumulo.core.client.Instance;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.log4j.Logger;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Utility to fetch the location (host:port) of the Accumulo monitor application.
 */
public class AccumuloMonitorLocator {
    private static final Logger LOGGER = Logger.getLogger(AccumuloMonitorLocator.class);
    
    private static final Charset ENCODING = StandardCharsets.UTF_8;
    private static final String MONITOR_HTTP_ADDR = "/accumulo/%s/monitor/http_addr";
    private static final int DEFAULT_NUM_RETRIES = 5;
    private static final int DEFAULT_RETRY_WAIT = 500;
    
    private RetryPolicy retryPolicy;
    
    public AccumuloMonitorLocator() {
        this(DEFAULT_NUM_RETRIES, DEFAULT_RETRY_WAIT);
    }
    
    public AccumuloMonitorLocator(int numRetries, int retryWaitMillis) {
        retryPolicy = new RetryNTimes(numRetries, retryWaitMillis);
    }
    
    /**
     * Fetches the monitor URL from the zookeeper used byt he given instance.
     *
     * @param instance
     *            the zookeeper instance to use for retrieving the monitor URL
     * @return the onitor url or null if not found
     */
    public String getUrl(Instance instance) {
        try (CuratorFramework curator = CuratorFrameworkFactory.newClient(instance.getZooKeepers(), retryPolicy)) {
            curator.start();
            byte[] bytes = curator.getData().forPath(String.format(MONITOR_HTTP_ADDR, instance.getInstanceID()));
            return new String(bytes, ENCODING);
        } catch (Exception e) {
            LOGGER.error("Cloud not fetch Accumulo monitor URL from zookeeper", e);
            throw new IllegalStateException("Could not fetch Accumulo monitor URL from zookeeper", e);
        }
    }
}
