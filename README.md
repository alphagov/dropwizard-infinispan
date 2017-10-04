# dropwizard-infinispan

Dropwizard bundle and configuration for [Infinispan](https://infinispan.org).

## Using the library

* Add the dependency to your project: `uk.gov.ida:dropwizard-infinispan`.
* Add the bundle to your app: `bootstrap.addBundle(new InfinispanBundle())`.
* Configure Infinispan in the application config file:

```yaml
infinispan:
  type: clustered|insecure|standalone
```

There are also the following optional configuration values:

```yaml
infinispan:
  bindAddress: 127.0.0.1
  port: 9090
  initialHosts: host
  clusterName: cluster
  expiration: 30m
  authConfiguration: 
    authValue: x
    keyStorePath: x
    keyStorePassword: x
    keyStoreType: x
    certAlias: x
    cipherType: x
  encryptConfiguration:
    keyStoreName: x
    keyStorePassword: x
    encryptionKeyAlias: x
  persistenceToFileEnabled: true
  persistenceFileLocation: /tmp/cache
```

## Licence

[MIT License](LICENCE)

## Versioning policy

dropwizard-infinispan-[dropwizard version]-[build number]

