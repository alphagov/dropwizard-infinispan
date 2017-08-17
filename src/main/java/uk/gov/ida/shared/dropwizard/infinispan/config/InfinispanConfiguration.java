package uk.gov.ida.shared.dropwizard.infinispan.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.ValidationMethod;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Configuration for an Infinispan embedded cache.
 * <p/>
 * Provides overrides for the jgroups.xml file in the resources section
 * Can either be 'clustered' or 'standalone' depending on the needs of the service
 */
public class InfinispanConfiguration {

    private static final int DEFAULT_PORT = 7800;

    @SuppressWarnings("unused") // needed for JAXB
    private InfinispanConfiguration() {
    }

    public InfinispanConfiguration(
            Optional<String> bindAddress,
            int port,
            Optional<String> initialHosts,
            Optional<String> clusterName,
            CacheType type,
            Optional<Duration> expiration,
            Optional<AuthConfiguration> authConfiguration,
            Optional<EncryptConfiguration> encryptConfiguration,
            Optional<Boolean> persistenceToFileEnabled,
            Optional<String> persistenceFileLocation) {

        this.bindAddress = bindAddress;
        this.port = port;
        this.initialHosts = initialHosts;
        this.clusterName = clusterName;
        this.type = type;
        this.expiration = expiration;
        this.encryptConfiguration = encryptConfiguration;
        this.authConfiguration = authConfiguration;
        this.persistenceToFileEnabled = persistenceToFileEnabled;
        this.persistenceFileLocation = persistenceFileLocation;
    }

    @JsonProperty
    @Valid
    private Optional<String> bindAddress = Optional.absent();

    @JsonProperty
    private int port = DEFAULT_PORT;

    @JsonProperty
    @Valid
    private Optional<String> initialHosts = Optional.absent();

    @JsonProperty
    private Optional<String> clusterName = Optional.absent();

    @JsonProperty
    @NotNull
    private CacheType type;

    @JsonProperty
    private Optional<Duration> expiration = Optional.absent();

    @JsonProperty
    @Valid
    private Optional<AuthConfiguration> authConfiguration = Optional.absent();

    @JsonProperty
    @Valid
    private Optional<EncryptConfiguration> encryptConfiguration = Optional.absent();

    @JsonProperty
    private Optional<Boolean> persistenceToFileEnabled = Optional.absent();

    @JsonProperty
    private Optional<String> persistenceFileLocation = Optional.absent();

    @JsonProperty
    private boolean fetchInMemoryState = true;

    @JsonProperty
    private boolean awaitInitialTransfer = true;

    @JsonProperty
    private long stateTransferTimeout = 5;

    public String getBindAddress() {
        return bindAddress.or("[no bind address provided]");
    }

    public int getPort() {
        return port;
    }

    public String getInitialHosts() {
        return initialHosts.or("[no hosts provided]");
    }

    public String getClusterName() {
        return clusterName.or("[no cluster name provided]");
    }

    public CacheType getType() {
        return type;
    }

    public Optional<Duration> getExpiration() {
        return expiration;
    }

    public Optional<AuthConfiguration> getAuthConfiguration() {
        return authConfiguration;
    }

    public Optional<EncryptConfiguration> getEncryptConfiguration() {
        return encryptConfiguration;
    }

    public Optional<Boolean> getPersistenceToFileEnabled() {
        return persistenceToFileEnabled;
    }

    public Optional<String> getPersistenceFileLocation() {
        return persistenceFileLocation;
    }

    @ValidationMethod(message = "Infinispan Configuration is not valid - check documentation")
    public boolean isValid() { // must start with an 'is' due to a daft JavaBeans convention (http://dropwizard.codahale.com/manual/core/#man-core-representations)
        switch (getType()) {
            case clustered:
            case insecure:
                return check(clusterName) && check(bindAddress) && check(initialHosts) && validatePersistenceFilenameIsPresentIfPersistenceEnabled();
            case standalone:
                return validatePersistenceFilenameIsPresentIfPersistenceEnabled();
        }

        return false; // this shouldn't happen
    }

    private boolean validatePersistenceFilenameIsPresentIfPersistenceEnabled() {
        if (persistenceToFileEnabled.isPresent()){
            if(persistenceToFileEnabled.get()){
                return persistenceFileLocation.isPresent() && !persistenceFileLocation.get().equals("");
            }
        }
        return true;
    }

    @ValidationMethod(message = "Infinispan Auth Configuration is not valid - check documentation")
    public boolean isAuthValid() {
        return getType() != CacheType.clustered || authConfiguration.isPresent();
    }

    @ValidationMethod(message = "Infinispan Encrypt Configuration is not valid - check documentation")
    public boolean isEncryptValid() {
        return getType() != CacheType.clustered || encryptConfiguration.isPresent();
    }
    private boolean check(Optional<String> optionalString) {
        return optionalString.isPresent() && !Strings.isNullOrEmpty(optionalString.get());
    }

    public boolean getFetchInMemoryState() {
       return fetchInMemoryState;
    }

    public boolean getAwaitInitialTransfer() {
        return awaitInitialTransfer;
    }

    public long getStateTransferTimeout() {
        return stateTransferTimeout;
    }
}
