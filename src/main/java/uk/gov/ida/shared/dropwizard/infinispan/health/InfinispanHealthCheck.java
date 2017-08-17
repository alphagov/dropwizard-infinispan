package uk.gov.ida.shared.dropwizard.infinispan.health;

import com.codahale.metrics.health.HealthCheck;
import uk.gov.ida.shared.dropwizard.infinispan.config.CacheType;
import uk.gov.ida.shared.dropwizard.infinispan.config.InfinispanConfiguration;
import uk.gov.ida.shared.dropwizard.infinispan.config.InfinispanServiceConfiguration;
import uk.gov.ida.shared.dropwizard.infinispan.util.InfinispanCacheManager;

import java.text.MessageFormat;

public class InfinispanHealthCheck extends HealthCheck {
    private InfinispanServiceConfiguration configuration;
    private final InfinispanCacheManager infinispanCacheManager;

    /**
     * Create a new {@link com.codahale.metrics.health.HealthCheck} instance with the given name.
     */
    public InfinispanHealthCheck(InfinispanServiceConfiguration configuration, InfinispanCacheManager infinispanCacheManager) {
        this.configuration = configuration;
        this.infinispanCacheManager = infinispanCacheManager;
    }

    @Override
    protected Result check() {
        InfinispanConfiguration infinispanConfiguration = configuration.getInfinispan();
        if (infinispanConfiguration.getType() == CacheType.standalone) {
            return Result.healthy();
        }

        if (!infinispanCacheManager.checkNode()) {
            return Result.unhealthy("This node's Infinispan is not in a running state!");
        } else if (!infinispanCacheManager.checkCluster()) {
            return Result.unhealthy("Something is wrong with the Infinispan Cluster. This node thinks it's the only node in the cluster.");
        }
        return Result.healthy(MessageFormat.format("Node Count: {0}", infinispanCacheManager.getNodeCount()));
    }
}
