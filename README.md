[![CircleCI](https://circleci.com/gh/Deutsche-Boerse-Risk/DAVe.svg?style=shield)](https://circleci.com/gh/Deutsche-Boerse-Risk/DAVe) 
[![Coverage Status](https://coveralls.io/repos/github/Deutsche-Boerse-Risk/DAVe/badge.svg?branch=master)](https://coveralls.io/github/Deutsche-Boerse-Risk/DAVe?branch=master) 
[![codebeat badge](https://codebeat.co/badges/54fe7c25-2329-4b95-8172-f80a01611474)](https://codebeat.co/projects/github-com-deutsche-boerse-risk-dave) 
[![Dependency Status](https://dependencyci.com/github/Deutsche-Boerse-Risk/DAVe/badge)](https://dependencyci.com/github/Deutsche-Boerse-Risk/DAVe) 
[![SonarQube](https://sonarqube.com/api/badges/gate?key=com.deutscheboerse.risk:dave)](https://sonarqube.com/dashboard/index/com.deutscheboerse.risk:dave)

# DAVe

**DAVe** is **D**ata **A**nalytics and **V**isualisation S**e**rvice. It provides REST interface to access latest risk and margining data from Eurex Clearing Prisma.

## Build

```
mvn clean package
```

The shippable artifact will be built in `target/dave-VERSION` directory.

## Configure

Configuration is stored in `dave.conf` file in Hocon format. Configuration is split into several sections:

### StoreManager

The `storeManager` section contains the configuration of the Store Manager service where the data are persisted.

| Option | Explanation | Example |
|--------|-------------|---------|
| `hostname` | Hostname of the DAVe-StoreManager | `localhost` |
| `port` | Port where the DAVe-StoreManager is listening to HTTPS connections | `8443` |
| `sslKey` | Private key of the DAVe-API | |
| `sslCert` | Public key of the DAVe-API | |
| `sslTrustCerts` | List of trusted certification authorities | |


### API

The `api` section configures the web based UI and the REST API.

| Option | Explanation | Example |
|--------|-------------|---------|
| `port` | Port of the HTTP(S) server | `8443` |
| `compression` | Enable gzip compression of the HTTP responses | `true` |
| `ssl` | Subsection configuring SSL/TLS on the webserver |  |
| `cors` | Subsection configuring Cross-origin resource sharing (see below) |  |
| `csrf` | Subsection configuring Cross-site request forgery protection (see below) |  |
| `auth` | Subsection configuring authentication (see below) |  |


#### SSL

The `ssl` section configures the SSL/TLS support in the webserver.

| Option | Explanation | Example |
|--------|-------------|---------|
| `enable` | Enable HTTPS protocol | `true` |
| `sslKey` | Private key of DAVe-API | |
| `sslCert` | Public certificate of DAVe-API server which clients trust | |
| `sslRequireClientAuth` | Sets TLS client authentication as required | `false` |
| `sslTrustCerts` | If TLS client authentication is required then this field contains list of trusted client certificates | `[]` |

#### Auth

The `auth` subsection configures authentication to the UI and REST interface.

| Option | Explanation | Example |
|--------|-------------|---------|
| `enable` | Disables or enables authentication | `true` |
| `clientId` | Client ID for the OpenID backend| `dave-ui` |
| `wellKnownUrl` | URL of `.well-known/openid-configuration` endpoint of the OpenID backend | `https://auth.dave.dbg-devops.com/auth/realms/DAVe/.well-known/openid-configuration` |

#### CORS

The `cors` subsection configures Cross-origin resource sharing (CORS), which allows the REST API to be used from web applications running under different domain.

| Option | Explanation | Example |
|--------|-------------|---------|
| `enable` | Enabled the CORS handler | `true` |
| `origin` | Configures the domain from which cross-origin access will be allowed | `http://mydomain.com` |

#### CSRF

The `csrf` subsection configures Cross-site request forgery (CSRF) protection. When enabled, the handler will set a XSRF-TOKEN cookie and the client has to send back its value in the X-XSRF-TOKEN header. This handler weill be activated only when authentication is enabled.

| Option | Explanation | Example |
|--------|-------------|---------|
| `enable` | Enable the CSRF handler | `true` |
| `secret` | Configures the domain secret used to generate CSFR tokens | `61d77a85-276b-476a-8810-f8408b5cfa19` |

### Health Check

The `healthCheck` section contains configuration where the REST API for checking the health/readiness status of the
microservice will be published.

| Option | Explanation | Example |
|--------|-------------|---------|
| `port` | Port of the HTTP server hosting REST API | 8080 |

The REST API provides two endpoints for checking the state using HTTP GET method:
- /healthz
- /readiness

## Run

Use script `start.sh` to start the application.

### Docker image to run standalone API
[DAVe-API Docker image](docker)

