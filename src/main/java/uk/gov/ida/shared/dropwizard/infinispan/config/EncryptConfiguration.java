package uk.gov.ida.shared.dropwizard.infinispan.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class EncryptConfiguration {

    @JsonProperty
    @NotNull
    private String keyStoreName;

    @JsonProperty
    @NotNull
    private String keyStorePassword;

    @JsonProperty
    @NotNull
    private String encryptionKeyAlias;

    public String getKeyStoreName() {
        return keyStoreName;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getEncryptionKeyAlias() {
        return encryptionKeyAlias;
    }
}
