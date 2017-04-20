[![CircleCI](https://circleci.com/gh/Deutsche-Boerse-Risk/DAVe.svg?style=shield)](https://circleci.com/gh/Deutsche-Boerse-Risk/DAVe) [![Build Status](https://travis-ci.org/Deutsche-Boerse-Risk/DAVe.svg?branch=master)](https://travis-ci.org/Deutsche-Boerse-Risk/DAVe) [![Coverage Status](https://coveralls.io/repos/github/Deutsche-Boerse-Risk/DAVe/badge.svg?branch=master)](https://coveralls.io/github/Deutsche-Boerse-Risk/DAVe?branch=master) [![codebeat badge](https://codebeat.co/badges/54fe7c25-2329-4b95-8172-f80a01611474)](https://codebeat.co/projects/github-com-deutsche-boerse-risk-dave) [![Dependency Status](https://dependencyci.com/github/Deutsche-Boerse-Risk/DAVe/badge)](https://dependencyci.com/github/Deutsche-Boerse-Risk/DAVe) [![SonarQube](https://sonarqube.com/api/badges/gate?key=com.deutscheboerse.risk:dave)](https://sonarqube.com/dashboard/index/com.deutscheboerse.risk:dave)

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
| `verifyHost` | Flag for verification of the DAVe-StoreManager hostname | false |
| `sslKey` | Private key of the DAVe-API | |
| `sslCert` | Public key of the DAVe-API | |
| `sslTrustCerts` | List of trusted certification authorities | |
| `restApi` | Subsection defining REST API for storing every model (see next table) |  |

#### RestApi

The `restApi` section contains the configuration of DAVe-StoreManager REST API points.

| Option | Explanation | Example |
|--------|-------------|---------|
| `accountMargin` | REST API address for querying account margin data | `/api/v1.0/query/am` |
| `liquiGroupMargin` | REST API address for querying liqui group margin data | `/api/v1.0/query/lgm` |
| `liquiGroupSplitMargin` | REST API address for querying liqui group split margin data | `/api/v1.0/query/lgsm` |
| `poolMargin` | REST API address for querying pool margin data | `/api/v1.0/query/pm` |
| `positionReport` | REST API address for querying position report data | `/api/v1.0/query/pr` |
| `riskLimitUtilization` | REST API address for querying risk limit utilization data | `/api/v1.0/query/rlu` |
| `healthz` | REST API address for find out whether the Store Manager is running | `/healthz` |

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
| `jwtPublicKey` | Public key for verification of received JWT tokens | |
| `permissionsClaimKey` | Path to roles inside JWT token | `realm_access/roles` |

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

## Run

Use script `start.sh` to start the application.

### Docker image to run standalone API
[DAVe-API Docker image](docker)

