package uk.gov.ida.shared.dropwizard.infinispan.integration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.shared.dropwizard.infinispan.config.InfinispanConfiguration;
import uk.gov.ida.shared.dropwizard.infinispan.config.InfinispanServiceConfiguration;
import uk.gov.ida.shared.dropwizard.infinispan.util.InfinispanBundle;

import javax.validation.constraints.NotNull;
import javax.ws.rs.client.Client;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class InfinispanDetailsIntegrationTest {

    public static class TestApplication extends Application<TestConfiguration>{

        @Override
        public void initialize(Bootstrap<TestConfiguration> bootstrap) {
            bootstrap.addBundle(new InfinispanBundle());
        }

        @Override
        public void run(TestConfiguration configuration, Environment environment) throws Exception {

        }
    }

    private static class TestConfiguration extends Configuration implements InfinispanServiceConfiguration {

        private InfinispanConfiguration infinispan;

        @Override
        public InfinispanConfiguration getInfinispan() {
            return infinispan;
        }
    }

    static class InfinispanDetailsResponse {
        @JsonProperty @NotNull private String address;
        @JsonProperty @NotNull List<String> members;
        @JsonProperty @NotNull String clusterName;
        @JsonProperty @NotNull int clusterSize;
        @JsonProperty @NotNull int expectedClusterSize;
        @JsonProperty @NotNull String coordinator;
        @JsonProperty @NotNull String status;
        @JsonProperty @NotNull List<String> cacheNames;
    }

    @ClassRule
    public static DropwizardAppRule<TestConfiguration> appRule = new DropwizardAppRule<TestConfiguration>(TestApplication.class, ResourceHelpers.resourceFilePath("test_application.yml"));

    @Test
    public void shouldDoSomething() throws Exception {
        final Client client = new JerseyClientBuilder(appRule.getEnvironment()).build("test client");
        final javax.ws.rs.core.Response response = client.target(String.format("http://localhost:%d/infinispan/details/", appRule.getLocalPort())).request().get();
        final InfinispanDetailsResponse infinispanDetails = response.readEntity(InfinispanDetailsResponse.class);
        assertThat(infinispanDetails.address).isEqualTo("null");
        assertThat(infinispanDetails.members).containsOnly("NO MEMEBERS");
        assertThat(infinispanDetails.clusterName).isEqualTo("ISPN");
        assertThat(infinispanDetails.clusterSize).isEqualTo(1);
        assertThat(infinispanDetails.expectedClusterSize).isEqualTo(1);
        assertThat(infinispanDetails.coordinator).isEqualTo("null");
        assertThat(infinispanDetails.status).isEqualTo("INSTANTIATED");
        assertThat(infinispanDetails.cacheNames).isEqualTo(Collections.emptyList());
    }
}
