package uk.gov.ida.shared.dropwizard.infinispan.util;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import org.apache.commons.lang3.StringUtils;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.Address;
import uk.gov.ida.shared.dropwizard.infinispan.config.CacheType;
import uk.gov.ida.shared.dropwizard.infinispan.config.InfinispanConfiguration;

import javax.annotation.Nullable;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

@Path("/infinispan/details")
@Produces("application/json")
public class InfinispanDetailsResource {

    private final InfinispanCacheManager infinispanCacheManager;
    private final InfinispanConfiguration infinispanConfiguration;

    public InfinispanDetailsResource(InfinispanCacheManager infinispanCacheManager, InfinispanConfiguration infinispanConfiguration) {
        this.infinispanCacheManager = infinispanCacheManager;
        this.infinispanConfiguration = infinispanConfiguration;
    }

    @GET
    public InfinispanDetails getInfinispanDetails(){

        return new InfinispanDetails(infinispanCacheManager.getEmbeddedCacheManager(), infinispanConfiguration);
    }

    public static class InfinispanDetails {

        private final EmbeddedCacheManager cacheManager;
        private final InfinispanConfiguration infinispanConfiguration;

        public InfinispanDetails(EmbeddedCacheManager cacheManager, InfinispanConfiguration infinispanConfiguration) {
            this.cacheManager = cacheManager;
            this.infinispanConfiguration = infinispanConfiguration;
        }

        public int getClusterSize() {
            final List<Address> members = cacheManager.getMembers();
            if(cacheManager.getMembers() == null) {
               return 1;
            }
            return members.size();
        }

        public int getExpectedClusterSize() {
            if (infinispanConfiguration.getType() == CacheType.standalone){
                return 1;
            } else {
                return StringUtils.countMatches(infinispanConfiguration.getInitialHosts(), ",") + 1;
            }
        }

        public String getClusterName() {
            return cacheManager.getClusterName();
        }

        public Set<String> getCacheNames() {
            return cacheManager.getCacheNames();
        }

        public String getStatus() {
            return cacheManager.getStatus().toString();
        }

        public Collection<String> getMembers() {
            return Optional.fromNullable(cacheManager.getMembers()).transform(new Function<List<Address>, Collection<String>>() {
                @Nullable
                @Override
                public Collection<String> apply(@Nullable List<Address> input) {
                    return Collections2.transform(input, new Function<Address, String>() {
                        @Nullable
                        @Override
                        public String apply(Address input) {
                            return input.toString();
                        }
                    });
                }
            }).or(asList("NO MEMEBERS"));
        }

        public String getAddress() {
            return String.valueOf(cacheManager.getAddress());
        }

        public String getCoordinator() {
            return String.valueOf(cacheManager.getCoordinator());
        }


    }
}
