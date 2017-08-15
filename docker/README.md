# DAVe Docker image

**DAVe** docker image allows DAVe to be executed in Docker / Kubernetes. It contains an entrypoint which will take case of the configuration based on environment variables. The different options are described below.

## Examples

To run DAVe in Docker, you have to pass the environment variables to the `docker run` command.

`docker run -ti -P -e API_COMPRESSION=1 -e API_AUTH=1 -e API_SSL_CERT="$apiCrt" -e API_SSL_KEY="$apiKey" 
-e STOREMANAGER_HOSTNAME=172.17.0.1 -e STOREMANAGER_PORT=27017 -e STOREMANAGER_SSL_CERT="$clientCrt" 
-e STOREMANAGER_SSL_KEY="$clientKey" dbgdave/dave-api:latest`

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
| `LOG_LEVEL` | Logging level which should be used | `info` |


### StoreManager

| Option | Explanation | Example |
|--------|-------------|---------|
| `STOREMANAGER_HOSTNAME` | Hostname where DAVe Store Manager is running | `localhost` |
| `STOREMANAGER_PORT` | Port where DAVe Store Manager is listening to SSL connections | 8443 |
| `STOREMANAGER_SSL_KEY` | Private key of the client in PEM format | |
| `STOREMANAGER_SSL_CERT` | Public key of the client CRT format | |
| `STOREMANAGER_SSL_TRUST_CERTS` | List of trusted certification authorities | |


### API Server

| Option | Explanation | Example |
|--------|-------------|---------|
| `API_COMPRESSION` | Enable compression of HTTP responses | `1` |
| `API_CORS` | Enable Cross-origin resource sharing | `1` |
| `API_CORS_ORIGIN` | Set the Cross-origin resource sharing origin host | `myhost.mydomain.tld` |
| `API_CSRF` | Enable the Cross-site request forgery handler | `1` |
| `API_CSRF_SECRET` | Configures the domain secret used to generate CSFR tokens | `61d77a85-276b-476a-8810-f8408b5cfa19` |
| `API_SSL` | Enable HTTPS protocol | `1`|
| `API_SSL_KEY` | Private key of the HTTP server in PEM format | |
| `API_SSL_CERT` | Public key of the HTTP server | |
| `API_SSL_TRUST_CERTS` | List of trusted CA for SSL client authentication | |
| `API_SSL_REQUIRE_CLIENT_AUTH` | Make SSL Client Authentication required | `1` |
| `API_AUTH` | Enable authentication | `1` |
| `API_AUTH_CLIENT_ID` | Client ID for the OpenID backend| `dave-ui` |
| `API_AUTH_WELL_KNOWN_URL` | URL of `.well-known/openid-configuration` endpoint of the OpenID backend | `https://auth.dave.dbg-devops.com/auth/realms/DAVe/.well-known/openid-configuration` |
| `API_AUTH_PERMISSIONS_CLAIM_KEY` | Path to roles inside JWT token | `realm_access/roles` |
