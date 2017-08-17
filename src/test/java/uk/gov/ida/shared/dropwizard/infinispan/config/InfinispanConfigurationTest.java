package uk.gov.ida.shared.dropwizard.infinispan.config;

import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.DefaultConfigurationFactoryFactory;
import io.dropwizard.jackson.Jackson;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.shared.dropwizard.infinispan.config.CacheType.clustered;

public class InfinispanConfigurationTest {

    private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private ConfigurationFactory<InfinispanConfiguration> configurationFactory;

    @Before
    public void setUp() {
        configurationFactory = new DefaultConfigurationFactoryFactory<InfinispanConfiguration>().create(
                InfinispanConfiguration.class,
                factory.getValidator(),
                Jackson.newObjectMapper(),
                "infinispan"
        );
    }

    private InfinispanConfiguration loadConfigurationFromYaml(String yaml) throws IOException, ConfigurationException {
        ConfigurationSourceProvider provider = path -> new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
        return configurationFactory.build(provider, "");
    }

    @Test
    public void validator_shouldValidateClusteredConfiguration() throws IOException, ConfigurationException {
        InfinispanConfiguration infinispanConfiguration = new InfinispanConfigurationBuilder().setType(clustered).build();

        assertThat(infinispanConfiguration.isValid()).isEqualTo(true);

        Set<ConstraintViolation<InfinispanConfiguration>> constraintViolations = runValidations(infinispanConfiguration);

        assertThat(constraintViolations.size()).isEqualTo(0);
    }

    @Test
    public void validator_shouldFailValidationIfBindAddressMissingFromClusteredConfiguration() {
        InfinispanConfiguration infinispanConfiguration = new InfinispanConfigurationBuilder()
                .setType(CacheType.clustered)
                .setBindAddress(absent())
                .build();

        Set<ConstraintViolation<InfinispanConfiguration>> constraintViolations = runValidations(infinispanConfiguration);

        assertThat(constraintViolations.size()).isEqualTo(1);
        assertThat(constraintViolations.iterator().next().getMessage()).isEqualTo("Infinispan Configuration is not valid - check documentation");
    }

    @Test
    public void validator_shouldFailValidationIfInitialHostsAreMissingFromClusteredConfiguration() {
        InfinispanConfiguration infinispanConfiguration = new InfinispanConfigurationBuilder()
                .setType(CacheType.clustered)
                .setInitialHosts(absent())
                .build();

        Set<ConstraintViolation<InfinispanConfiguration>> constraintViolations = runValidations(infinispanConfiguration);

        assertThat(constraintViolations.size()).isEqualTo(1);
        assertThat(constraintViolations.iterator().next().getMessage()).isEqualTo("Infinispan Configuration is not valid - check documentation");
    }

    @Test
    public void validator_shouldFailValidationIfClusterNameIsMissingFromClusteredConfiguration() {
        InfinispanConfiguration infinispanConfiguration = new InfinispanConfigurationBuilder()
                .setType(CacheType.clustered)
                .setClusterName(absent())
                .build();

        Set<ConstraintViolation<InfinispanConfiguration>> constraintViolations = runValidations(infinispanConfiguration);

        assertThat(constraintViolations.size()).isEqualTo(1);
        assertThat(constraintViolations.iterator().next().getMessage()).isEqualTo("Infinispan Configuration is not valid - check documentation");
    }

    @Test
    public void validator_shouldAllowEmptyExpiration() throws IOException, ConfigurationException {
        InfinispanConfiguration infinispanConfiguration = loadConfigurationFromYaml(
                "type: standalone\n" +
                "persistenceToFileEnabled: true\n" +
                "persistenceFileLocation: bla/bla"
        );

        Set<ConstraintViolation<InfinispanConfiguration>> constraintViolations = runValidations(infinispanConfiguration);

        assertThat(constraintViolations.size()).isEqualTo(0);
        assertThat(infinispanConfiguration.getExpiration().isPresent()).isFalse();
    }

    @Test
    public void validator_shouldFailValidationIfAuthConfigIsMissingFromClusteredConfiguration() {
        InfinispanConfiguration infinispanConfiguration = new InfinispanConfigurationBuilder()
                .setType(CacheType.clustered)
                .setAuthConfiguration(absent())
                .build();

        Set<ConstraintViolation<InfinispanConfiguration>> constraintViolations = runValidations(infinispanConfiguration);

        assertThat(constraintViolations.size()).isEqualTo(1);
        assertThat(constraintViolations.iterator().next().getMessage()).isEqualTo("Infinispan Auth Configuration is not valid - check documentation");
    }

    @Test
    public void validator_shouldFailValidationIfEncryptConfigIsMissingFromClusteredConfiguration() {
        InfinispanConfiguration infinispanConfiguration = new InfinispanConfigurationBuilder()
                .setType(CacheType.clustered)
                .setEncryptConfiguration(absent())
                .build();

        Set<ConstraintViolation<InfinispanConfiguration>> constraintViolations = runValidations(infinispanConfiguration);

        assertThat(constraintViolations.size()).isEqualTo(1);
        assertThat(constraintViolations.iterator().next().getMessage()).isEqualTo("Infinispan Encrypt Configuration is not valid - check documentation");
    }

    @Test
    public void isValid_shouldValidateStandaloneConfiguration() throws Exception {
        InfinispanConfiguration infinispanConfiguration = new InfinispanConfigurationBuilder()
                .setClusterName(absent())
                .setBindAddress(absent())
                .setAuthConfiguration(absent())
                .setEncryptConfiguration(absent())
                .setPersistenceFileLocation(absent())
                .setPersistenceToFileEnabled(absent())
                .build();

        assertThat(infinispanConfiguration.isValid()).isEqualTo(true);
    }

    @Test
    public void shouldFailValidation_ifPersistenceIsOnAndNoFileNameHasBeenProvided() {
        InfinispanConfiguration infinispanConfiguration = new InfinispanConfigurationBuilder()
                .setPersistenceToFileEnabled(of(true))
                .setPersistenceFileLocation(absent())
                .build();
        assertThat(infinispanConfiguration.isValid()).isEqualTo(false);
    }

    private static <T> Set<ConstraintViolation<T>> runValidations(T data) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        return validator.validate(data);
    }
}
