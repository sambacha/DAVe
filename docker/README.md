# DAVe Docker image

**DAVe** docker image allows DAVe to be executed in Docker / Kubernetes. It contains an entrypoint which will take case of the configuration based on environment variables. The different options are described below.

## Examples

To run DAVe in Docker, you have to pass the environment variables to the `docker run` command.

`docker run -ti -P -e DAVE_HTTP_COMPRESSION=1 -e DAVE_HTTP_AUTH=1 -e DAVE_HTTP_SSL_SERVER_PUBLIC_KEY="$serverCrt" -e DAVE_HTTP_
SSL_SERVER_PRIVATE_KEY="$serverKey" -e DAVE_STOREMANAGER_HOSTNAME=172.17.0.1 -e DAVE_STOREMANAGER_PORT=27017 
-e DAVE_STOREMANAGER_SSL_CLIENT_PUBLIC_KEY="$clientCrt" -e DAVE_STOREMANAGER_SSL_CLIENT_PRIVATE_KEY="$clientKey" dbgdave/dave-api:latest`

To actually use the application, you have to point to a host running the StoreManager service.

## Options

### General

| Option | Explanation | Example |
|--------|-------------|---------|
| `JAVA_OPTS` | JVM options | `-Xmx=512m` |


### Logging

Allows to configure logging parameters. Supported log levels are `off`, `error`, `warn`, `info`, `debug`, `trace` and `all`.

| Option | Explanation | Example |
|--------|-------------|---------|
| `DAVE_LOG_LEVEL` | Logging level which should be used | `info` |


### StoreManager

| Option | Explanation | Example |
|--------|-------------|---------|
| `DAVE_STOREMANAGER_HOSTNAME` | Hostname where DAVe-StoreManager is running | `localhost` |
| `DAVE_STOREMANAGER_PORT` | Port of the DAVe-StoreManager server | 8443 |
| `DAVE_STOREMANAGER_HEALTHCHECK_PORT` | DAVe-StoreManager's healthcheck port | 8080 |
| `DAVE_STOREMANAGER_SSL_CLIENT_PRIVATE_KEY` | Private key of the StoreManager client in PEM format | |
| `DAVE_STOREMANAGER_SSL_CLIENT_PUBLIC_KEY` | Public certificate of the StoreManager client | |
| `DAVE_STOREMANAGER_SSL_CLIENT_AUTH` | Whether SSL client Authentication is required | `1` |
| `DAVE_STOREMANAGER_SSL_VERIFY_HOST` | Verify host of StoreManager | `1` |
| `DAVE_STOREMANAGER_SSL_TRUST_CERTS` | List of trusted CA for SSL client authentication | |

### HTTP Server

| Option | Explanation | Example |
|--------|-------------|---------|
| `DAVE_HTTP_COMPRESSION` | Enable compression of HTTP responses | `1` |
| `DAVE_HTTP_CORS` | Enable Cross-origin resource sharing | `1` |
| `DAVE_HTTP_CORS_ORIGIN` | Set the Cross-origin resource sharing origin host | `myhost.mydomain.tld` |
| `DAVE_HTTP_CSRF` | Enable the Cross-site request forgery handler | `1` |
| `DAVE_HTTP_CSRF_SECRET` | Configures the domain secret used to generate CSFR tokens | `61d77a85-276b-476a-8810-f8408b5cfa19` |
| `DAVE_HTTP_SSL` | Enable HTTPS protocol | `1`|
| `DAVE_HTTP_SSL_SERVER_PRIVATE_KEY` | Private key of the HTTP server in PEM format | |
| `DAVE_HTTP_SSL_SERVER_PUBLIC_KEY` | Public key of the HTTP server | |
| `DAVE_HTTP_SSL_TRUST_CERTS` | List of trusted CA for SSL client authentication | |
| `DAVE_HTTP_SSL_CLIENT_AUTH` | Make SSL Client Authentication required | `1` |
| `DAVE_HTTP_AUTH` | Enable authentication | `1` |
| `DAVE_HTTP_AUTH_PUBLIC_KEY` | Public key for verification of received JWT tokens | |
| `DAVE_HTTP_AUTH_PERMISSIONS_CLAIM_KEY` | Path to roles inside JWT token | `realm_access/roles` |
