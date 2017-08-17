package uk.gov.ida.shared.dropwizard.infinispan.config;

import io.dropwizard.util.Duration;

import com.google.common.base.Optional;
import static org.mockito.Mockito.mock;

class InfinispanConfigurationBuilder {
    private Optional<String> bindAddress = Optional.of("val");
    private int port = 7800;
    private Optional<String> initialHosts = Optional.of("val");
    private Optional<String> clusterName = Optional.of("val");
    private CacheType type = CacheType.standalone;
    private Optional<Duration> expiration = Optional.of(Duration.days(1));
    private Optional<AuthConfiguration> authConfiguration = Optional.of(mock(AuthConfiguration.class));
    private Optional<EncryptConfiguration> encryptConfiguration = Optional.of(mock(EncryptConfiguration.class));
    private Optional<Boolean> persistenceToFileEnabled = Optional.of(false);
    private Optional<String> persistenceFileLocation = Optional.of("val");

    InfinispanConfiguration build() {
        return new InfinispanConfiguration(bindAddress, port, initialHosts, clusterName, type, expiration,
                authConfiguration, encryptConfiguration, persistenceToFileEnabled, persistenceFileLocation
        );
    }

    InfinispanConfigurationBuilder setBindAddress(Optional<String> bindAddress) {
        this.bindAddress = bindAddress;
        return this;
    }

    InfinispanConfigurationBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    InfinispanConfigurationBuilder setInitialHosts(Optional<String> initialHosts) {
        this.initialHosts = initialHosts;
        return this;
    }

    InfinispanConfigurationBuilder setClusterName(Optional<String> clusterName) {
        this.clusterName = clusterName;
        return this;
    }

    InfinispanConfigurationBuilder setType(CacheType type) {
        this.type = type;
        return this;
    }

    InfinispanConfigurationBuilder setExpiration(Optional<Duration> expiration) {
        this.expiration = expiration;
        return this;
    }

    InfinispanConfigurationBuilder setAuthConfiguration(Optional<AuthConfiguration> authConfiguration) {
        this.authConfiguration = authConfiguration;
        return this;
    }

    InfinispanConfigurationBuilder setEncryptConfiguration(Optional<EncryptConfiguration> encryptConfiguration) {
        this.encryptConfiguration = encryptConfiguration;
        return this;
    }

    InfinispanConfigurationBuilder setPersistenceToFileEnabled(Optional<Boolean> persistenceToFileEnabled) {
        this.persistenceToFileEnabled = persistenceToFileEnabled;
        return this;
    }

    InfinispanConfigurationBuilder setPersistenceFileLocation(Optional<String> persistenceFileLocation) {
        this.persistenceFileLocation = persistenceFileLocation;
        return this;
    }
}

