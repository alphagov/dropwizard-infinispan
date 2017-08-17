package uk.gov.ida.shared.dropwizard.infinispan.util;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.shared.dropwizard.infinispan.config.CacheType;
import uk.gov.ida.shared.dropwizard.infinispan.config.InfinispanConfiguration;
import uk.gov.ida.shared.dropwizard.infinispan.config.InfinispanServiceConfiguration;
import uk.gov.ida.shared.dropwizard.infinispan.health.InfinispanHealthCheck;

import javax.inject.Provider;
import java.util.concurrent.TimeUnit;

/**
 * A {@link ConfiguredBundle} for integrating with Infinispan.
 * <p/>
 * Includes a HealthCheck to Infinispan.
 * <p/>
 * To use this {@link ConfiguredBundle}, your configuration must implement
 * {@link InfinispanServiceConfiguration}.
 * <p/>
 * <h3>Example</h3>
 * <b>Configuration:</b>
 * <code>
 * class MyServiceConfiguration
 * extends Configuration
 * implements InfinispanServiceConfiguration {
 * <p/>
 * // service specific config
 * // ...
 * <p/>
 * \@JsonProperty
 * \@NotNull
 * private InfinispanConfiguration infinispan;
 * <p/>
 * \@Override
 * public InfinispanConfiguration getInfinispan() {
 * return infinispan;
 * }
 * }
 * </code>
 * <p/>
 * <b>Service:</b>
 * <code>
 * class MyService extends Service<MyServiceConfiguration> {
 * <p/>
 * public void initialize(Bootstrap<MyServiceConfiguration> bootstrap) {
 * bootstrap.addBundle(new InfinispanBundle());
 * }
 * <p/>
 * public void run(MyServiceConfiguration configuration,
 * Environment environment) throws Exception {
 * // ...
 * }
 * }
 * </code>
 */
public class InfinispanBundle implements ConfiguredBundle<InfinispanServiceConfiguration> {

    private static final Logger LOG = LoggerFactory.getLogger(InfinispanBundle.class);
    private static final long INFINISPAN_CACHE_ENTRIES_NEVER_EXPIRE_MAGIC_VALUE = -1L;

    private InfinispanConfiguration infinispanConfiguration;
    private InfinispanCacheManager infinispanCacheManager;

    public InfinispanBundle() {
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
    }

    @Override
    public void run(InfinispanServiceConfiguration configuration, Environment environment) {
        infinispanConfiguration = configuration.getInfinispan();
        if (infinispanConfiguration.getType() == CacheType.clustered) {
            LOG.info("Running Infinispan as a clustered cache.");
            infinispanCacheManager = configureClusteredCache(environment, infinispanConfiguration, environment.metrics());
        } else if (infinispanConfiguration.getType() == CacheType.insecure) {
            LOG.info("Running Infinispan as a insecure clustered cache.");
            infinispanCacheManager = configureInsecureCache(environment, infinispanConfiguration, environment.metrics());
        } else {
            LOG.info("Running Infinispan as a standalone cache.");
            infinispanCacheManager = configureStandaloneCache(environment, environment.metrics());
        }
        environment.healthChecks().register("Infinispan Health Check", new InfinispanHealthCheck(configuration, infinispanCacheManager));

        environment.jersey().register(new InfinispanDetailsResource(infinispanCacheManager, infinispanConfiguration));


    }

    public Provider<InfinispanCacheManager> getInfinispanCacheManagerProvider() {
        return () -> infinispanCacheManager;
    }

    private InfinispanCacheManager configureStandaloneCache(Environment environment, MetricRegistry metrics) {

        InfinispanCacheManager cacheManager;

        if (infinispanConfiguration.getPersistenceToFileEnabled() != null &&
                infinispanConfiguration.getPersistenceToFileEnabled().isPresent() &&
                infinispanConfiguration.getPersistenceToFileEnabled().get()) {
            cacheManager = standaloneCacheManagerWithPersistence(metrics);
        } else {
            cacheManager = standaloneCacheManagerWithoutPersistence(metrics);
        }

        environment.lifecycle().manage(cacheManager);
        return cacheManager;
    }

    private InfinispanCacheManager configureClusteredCache(Environment environment, InfinispanConfiguration infinispanConfiguration, MetricRegistry metrics) {
        // check for null first?
        System.setProperty("jgroups.tcp.address", infinispanConfiguration.getBindAddress());
        System.setProperty("jgroups.tcp.port", String.valueOf(infinispanConfiguration.getPort()));
        System.setProperty("jgroups.tcpping.initial_hosts", infinispanConfiguration.getInitialHosts());
        if (infinispanConfiguration.getEncryptConfiguration() != null && infinispanConfiguration.getEncryptConfiguration().isPresent()) {
            System.setProperty("jgroups.encrypt.key_store_name", infinispanConfiguration.getEncryptConfiguration().get().getKeyStoreName());
            System.setProperty("jgroups.encrypt.store_password", infinispanConfiguration.getEncryptConfiguration().get().getKeyStorePassword());
            System.setProperty("jgroups.encrypt.alias", infinispanConfiguration.getEncryptConfiguration().get().getEncryptionKeyAlias());
        }
        if (infinispanConfiguration.getAuthConfiguration() != null && infinispanConfiguration.getAuthConfiguration().isPresent()) {
            System.setProperty("jgroups.auth.auth_value", infinispanConfiguration.getAuthConfiguration().get().getAuthValue());
            System.setProperty("jgroups.auth.keystore_path", infinispanConfiguration.getAuthConfiguration().get().getKeyStorePath());
            System.setProperty("jgroups.auth.keystore_password", infinispanConfiguration.getAuthConfiguration().get().getKeyStorePassword());
            System.setProperty("jgroups.auth.keystore_type", infinispanConfiguration.getAuthConfiguration().get().getKeyStoreType());
            System.setProperty("jgroups.auth.cert_alias", infinispanConfiguration.getAuthConfiguration().get().getCertAlias());
            System.setProperty("jgroups.auth.cipher_type", infinispanConfiguration.getAuthConfiguration().get().getCipherType());
        }

        InfinispanCacheManager cacheManager;

        if (infinispanConfiguration.getPersistenceToFileEnabled() != null &&
                infinispanConfiguration.getPersistenceToFileEnabled().isPresent() &&
                infinispanConfiguration.getPersistenceToFileEnabled().get()) {
            cacheManager = clusteredCacheManagerWithPersistence(infinispanConfiguration, metrics);
        } else {
            cacheManager = clusteredCacheManagerWithoutPersistence(infinispanConfiguration, metrics);
        }
        environment.lifecycle().manage(cacheManager);
        return cacheManager;
    }

    private InfinispanCacheManager configureInsecureCache(Environment environment, InfinispanConfiguration infinispanConfiguration, MetricRegistry metrics) {
        // check for null first?
        System.setProperty("jgroups.tcp.address", infinispanConfiguration.getBindAddress());
        System.setProperty("jgroups.tcp.port", String.valueOf(infinispanConfiguration.getPort()));
        System.setProperty("jgroups.tcpping.initial_hosts", infinispanConfiguration.getInitialHosts());

        InfinispanCacheManager cacheManager;

        if (infinispanConfiguration.getPersistenceToFileEnabled() != null &&
                infinispanConfiguration.getPersistenceToFileEnabled().isPresent() &&
                infinispanConfiguration.getPersistenceToFileEnabled().get()) {
            cacheManager = insecureCacheManagerWithPersistence(infinispanConfiguration, metrics);
        } else {
            cacheManager = insecureCacheManagerWithoutPersistence(infinispanConfiguration, metrics);
        }
        environment.lifecycle().manage(cacheManager);
        return cacheManager;
    }


    public InfinispanCacheManager getCacheManager() {
        return infinispanCacheManager;
    }

    public void enableJMX(ConfigurationBuilder configurationBuilder) {
        configurationBuilder.jmxStatistics().enable();
    }

    public void addExpiration(ConfigurationBuilder configurationBuilder) {
        long cacheExpiry = getInfinispanCacheExpiryValue(infinispanConfiguration.getExpiration());
        configurationBuilder.expiration().lifespan(cacheExpiry);
    }

    public void addPersistence(ConfigurationBuilder configurationBuilder) {
        configurationBuilder.persistence()
                .addSingleFileStore()
                .location(infinispanConfiguration.getPersistenceFileLocation().get());
    }

    private void addClustering(ConfigurationBuilder configurationBuilder) {
        configurationBuilder
                .clustering()
                .cacheMode(CacheMode.REPL_SYNC)
                .stateTransfer()
                .awaitInitialTransfer(infinispanConfiguration.getAwaitInitialTransfer())
                .fetchInMemoryState(infinispanConfiguration.getFetchInMemoryState())
                .timeout(infinispanConfiguration.getStateTransferTimeout(), TimeUnit.SECONDS);
    }

    private InfinispanCacheManager standaloneCacheManagerWithoutPersistence(final MetricRegistry metrics) {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        addExpiration(configurationBuilder);
        enableJMX(configurationBuilder);
        return new InfinispanCacheManager(
                metrics,
                new DefaultCacheManager(
                        configurationBuilder.build()
                )
        );
    }

    private InfinispanCacheManager standaloneCacheManagerWithPersistence(MetricRegistry metrics) {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        addPersistence(configurationBuilder);
        addExpiration(configurationBuilder);
        enableJMX(configurationBuilder);
        return new InfinispanCacheManager(
                metrics,
                new DefaultCacheManager(configurationBuilder.build())
        );
    }

    private InfinispanCacheManager clusteredCacheManagerWithPersistence(final InfinispanConfiguration infinispanConfiguration, final MetricRegistry metrics) {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        enableJMX(configurationBuilder);
        addPersistence(configurationBuilder);
        addExpiration(configurationBuilder);
        addClustering(configurationBuilder);
        return new InfinispanCacheManager(
                metrics,
                new DefaultCacheManager(
                        GlobalConfigurationBuilder.defaultClusteredBuilder()
                                .transport()
                                .defaultTransport()
                                .clusterName(infinispanConfiguration.getClusterName())
                                .addProperty("configurationFile", "jgroups.xml")
                                .build(),
                        configurationBuilder.build()
                )
        );
    }

    private InfinispanCacheManager clusteredCacheManagerWithoutPersistence(final InfinispanConfiguration infinispanConfiguration, final MetricRegistry metrics) {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        addExpiration(configurationBuilder);
        addClustering(configurationBuilder);
        enableJMX(configurationBuilder);
        return new InfinispanCacheManager(
                metrics,
                new DefaultCacheManager(
                        GlobalConfigurationBuilder.defaultClusteredBuilder()
                                .transport()
                                .defaultTransport()
                                .clusterName(infinispanConfiguration.getClusterName())
                                .addProperty("configurationFile", "jgroups.xml")
                                .build(),
                        configurationBuilder.build()
                )
        );
    }

    private InfinispanCacheManager insecureCacheManagerWithPersistence(final InfinispanConfiguration infinispanConfiguration, final MetricRegistry metrics) {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        addExpiration(configurationBuilder);
        addPersistence(configurationBuilder);
        addClustering(configurationBuilder);
        enableJMX(configurationBuilder);
        return new InfinispanCacheManager(
                metrics,
                new DefaultCacheManager(
                        GlobalConfigurationBuilder.defaultClusteredBuilder()
                                .transport()
                                .defaultTransport()
                                .clusterName(infinispanConfiguration.getClusterName())
                                .removeProperty("configurationFile")
                                .addProperty("configurationFile", "jgroups-insecure.xml")
                                .build(),
                        configurationBuilder.build()
                )
        );
    }

    private InfinispanCacheManager insecureCacheManagerWithoutPersistence(final InfinispanConfiguration infinispanConfiguration, final MetricRegistry metrics) {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        addExpiration(configurationBuilder);
        addClustering(configurationBuilder);
        enableJMX(configurationBuilder);
        return new InfinispanCacheManager(
                metrics,
                new DefaultCacheManager(
                        GlobalConfigurationBuilder.defaultClusteredBuilder()
                                .transport()
                                .defaultTransport()
                                .clusterName(infinispanConfiguration.getClusterName())
                                .removeProperty("configurationFile")
                                .addProperty("configurationFile", "jgroups-insecure.xml")
                                .build(),
                        configurationBuilder.build()
                )
        );
    }

    private long getInfinispanCacheExpiryValue(Optional<Duration> expiration) {
         return expiration.transform(Duration::toMilliseconds).or(INFINISPAN_CACHE_ENTRIES_NEVER_EXPIRE_MAGIC_VALUE);
    }
}
