# DAVe Docker image

**DAVe** docker image allows DAVe to be executed in Docker / Kubernetes. It contains an entrypoint which will take case of the configuration based on environment variables. The different options are described below.

## Examples

To run DAVe in Docker, you have to pass the environment variables to the `docker run` command.

`docker run -ti -P -e DAVE_HTTP_ENABLE=1 -e DAVE_HTTP_COMPRESSION=1 -e DAVE_HTTP_AUTH=1 -e DAVE_HTTP_SSL_SERVER_PUBLIC_KEY="$webCrt" -e DAVE_HTTP_
SSL_SERVER_PRIVATE_KEY="$webKey" -e DAVE_DB_HOSTNAME=172.17.0.1 -e DAVE_DB_PORT=27017 -e DAVE_DB_NAME=DAVe scholzj/dave:latest`

To actually use the application, you have to point to a host running the MongoDB database.

## Options

### Logging

Allows to configure logging parameters. Supported log levels are `OFF`, `ERROR`, `WARN`, `INFO`, `DEBUG`, `TRACE` and `ALL`.

| Option | Explanation | Example |
|--------|-------------|---------|
| `DAVE_LOG_LEVEL` | Logging level which should be used | `INFO` |


### StoreManager

| Option | Explanation | Example |
|--------|-------------|---------|
| `DAVE_STOREMANAGER_HOSTNAME` | Hostname where DAVe-StoreManger is running | `localhost` |
| `DAVE_STOREMANAGER_PORT` | Port of the DAVe-StoreManager server | 8081 |
| `DAVE_STOREMANAGER_SSL` | Enable HTTPS protocol | `1` |
| `DAVE_STOREMANAGER_SSL_CERT` | Public SSL Certificate of DAVe-StorageManager which DAVe-Api trusts | `-----BEGIN CERTIFICATE-----\nMIIBqARKgAwIBAgI1` |

### HTTP Server

| Option | Explanation | Example |
|--------|-------------|---------|
| `DAVE_HTTP_COMPRESSION` | Enable compression of HTTP responses | `1` |
| `DAVE_HTTP_CORS` | Enable Cross-origin resource sharing | `1` |
| `DAVE_HTTP_CORS_ORIGIN` | Set the Cross-origin resource sharing origin host | `myhost.mydomain.tld` |
| `DAVE_HTTP_CSRF` | Enable the Cross-site request forgery handler | `1` |
| `DAVE_HTTP_CSRF_SECRET` | Configures the domain secret used to generate CSFR tokens | `61d77a85-276b-476a-8810-f8408b5cfa19` |
| `DAVE_HTTP_SSL` | Enable HTTPS protocol | `1`|
| `DAVE_HTTP_SSL_KEY` | Private key of the HTTP server in PEM format | `-----BEGIN PRIVATE KEY-----\nMIICeAIBADANB` |
| `DAVE_HTTP_SSL_CERT` | Public certificate of the HTTP server | `-----BEGIN CERTIFICATE-----\nMIIBqTCC` |
| `DAVE_HTTP_SSL_TRUST_CERTS` | List of trusted CA for SSL client authentication | `-----BEGIN CERTIFICATE-----\nMIIBqTCCARKgAwIBAgIIT7GLwd` |
| `DAVE_HTTP_SSL_CLIENT_AUTH` | Make SSL Client Authentication required | `1` |
| `DAVE_HTTP_AUTH` | Enable authentication | `1` |
| `DAVE_HTTP_AUTH_PUBLIC_KEY` | Public key for verification of received JWT tokens | |
| `DAVE_HTTP_AUTH_PERMISSIONS_CLAIM_KEY` | Path to roles inside JWT token | `realm_access/roles` |
