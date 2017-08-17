package uk.gov.ida.shared.dropwizard.infinispan.util;

import com.codahale.metrics.JmxAttributeGauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Throwables;
import io.dropwizard.lifecycle.Managed;
import org.infinispan.Cache;
import org.infinispan.lifecycle.ComponentStatus;
import org.infinispan.manager.EmbeddedCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import static java.text.MessageFormat.format;

public class InfinispanCacheManager implements Managed {
    private static final Logger LOG = LoggerFactory.getLogger(InfinispanCacheManager.class);

    private EmbeddedCacheManager embeddedCacheManager;
    private MetricRegistry registry;

    public InfinispanCacheManager(MetricRegistry registry, EmbeddedCacheManager embeddedCacheManager) {
        this.registry = registry;
        this.embeddedCacheManager = embeddedCacheManager;
    }

    public <T1,T2> Cache<T1,T2> getCache(String cacheName){
        Cache<T1,T2> cache = embeddedCacheManager.getCache(cacheName, true);

        String jmxName = getJmxName(cache);

        addCacheStatistics(cacheName, jmxName, "numberOfEntries");
        addCacheStatistics(cacheName, jmxName, "averageReadTime");
        addCacheStatistics(cacheName, jmxName, "averageWriteTime");
        addCacheStatistics(cacheName, jmxName, "evictions");
        addCacheStatistics(cacheName, jmxName, "hits");
        addCacheStatistics(cacheName, jmxName, "misses");
        addCacheStatistics(cacheName, jmxName, "removeHits");
        addCacheStatistics(cacheName, jmxName, "removeMisses");
        addCacheStatistics(cacheName, jmxName, "stores");

        return cache;
    }

    private String getJmxName(Cache cache){
        String cacheName = cache.getName();
        String cacheModeString = cache.getCacheConfiguration().clustering().cacheMode().toString().toLowerCase();
        String extendedCacheString = ObjectName.quote(cacheName + "(" + cacheModeString + ")");
        String managerName = ObjectName.quote(embeddedCacheManager.getCacheManagerConfiguration().globalJmxStatistics().cacheManagerName());
        return String.format("org.infinispan:type=Cache,name=%1$s,manager=%2$s,component=Statistics", extendedCacheString, managerName);
    }

    private void addCacheStatistics(String cacheName, String jmxName, String attribute){
        try {
            registry.register(MetricRegistry.name("uk.gov.ida.infinispan", cacheName, attribute), new JmxAttributeGauge(ObjectName.getInstance(jmxName), attribute));
        } catch (MalformedObjectNameException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void start() {
        embeddedCacheManager.start();
    }

    @Override
    public void stop() {
        embeddedCacheManager.stop();
    }

    // package local on purpose :-)
    EmbeddedCacheManager getEmbeddedCacheManager(){
        return embeddedCacheManager;
    }

    public boolean checkCluster() {
        return embeddedCacheManager.getMembers().size() > 1;
    }

    public boolean checkNode() {
        if (embeddedCacheManager.getStatus() == ComponentStatus.RUNNING) {
            return true;
        }
        LOG.info(format("Infinispan is not in a running state instead has status {0}", embeddedCacheManager.getStatus()));
        return false;
    }

    public int getNodeCount() {
        return embeddedCacheManager.getMembers().size();
    }
}
