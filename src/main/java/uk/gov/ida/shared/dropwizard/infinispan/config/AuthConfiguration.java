package uk.gov.ida.shared.dropwizard.infinispan.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class AuthConfiguration {

    @JsonProperty
    @NotNull
    private String authValue;

    @JsonProperty
    @NotNull
    private String keyStorePath;

    @JsonProperty
    @NotNull
    private String keyStorePassword;

    @JsonProperty
    @NotNull
    private String keyStoreType;

    @JsonProperty
    @NotNull
    private String certAlias;

    @JsonProperty
    @NotNull
    private String cipherType;

    @SuppressWarnings("unused") // required by JAXB
    private AuthConfiguration() {
    }

    public String getAuthValue() {
        return authValue;
    }

    public String getKeyStorePath() {
        return keyStorePath;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public String getCertAlias() {
        return certAlias;
    }

    public String getCipherType() {
        return cipherType;
    }
}
