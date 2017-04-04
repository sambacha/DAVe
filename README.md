[![CircleCI](https://circleci.com/gh/Deutsche-Boerse-Risk/DAVe.svg?style=shield)](https://circleci.com/gh/Deutsche-Boerse-Risk/DAVe) [![Build Status](https://travis-ci.org/Deutsche-Boerse-Risk/DAVe.svg?branch=master)](https://travis-ci.org/Deutsche-Boerse-Risk/DAVe) [![Coverage Status](https://coveralls.io/repos/github/Deutsche-Boerse-Risk/DAVe/badge.svg?branch=master)](https://coveralls.io/github/Deutsche-Boerse-Risk/DAVe?branch=master) [![codebeat badge](https://codebeat.co/badges/54fe7c25-2329-4b95-8172-f80a01611474)](https://codebeat.co/projects/github-com-deutsche-boerse-risk-dave) [![Dependency Status](https://dependencyci.com/github/Deutsche-Boerse-Risk/DAVe/badge)](https://dependencyci.com/github/Deutsche-Boerse-Risk/DAVe) [![SonarQube](https://sonarqube.com/api/badges/gate?key=com.deutscheboerse.risk:dave)](https://sonarqube.com/dashboard/index/com.deutscheboerse.risk:dave)

# DAVe

**DAVe** is **D**ata **A**nalytics and **V**isualisation S**e**rvice. It provides REST interface to access latest risk and margining data from Eurex Clearing Prisma.

![DAVe - Dashboard](https://github.com/Deutsche-Boerse-Risk/DAVe/blob/master/doc/screenshots/dave-screenshots.gif "DAVe - Dashboard")

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
| `hostname` | Name of the host which will be used | `localhost` |
| `port` | Port of the Store Manager server | `8081` |

### RestApi

The `restApi` section contains the configuration of the Store Manager's REST API points.

| Option | Explanation | Example |
|--------|-------------|---------|
| `accountMargin` | REST API address for querying account margin data | `/api/v1.0/query/am` |
| `liquiGroupMargin` | REST API address for querying liqui group margin data | `/api/v1.0/query/lgm` |
| `liquiGroupSplitMargin` | REST API address for querying liqui group split margin data | `/api/v1.0/query/lgsm` |
| `poolMargin` | REST API address for querying pool margin data | `/api/v1.0/query/pm` |
| `positionReport` | REST API address for querying position report data | `/api/v1.0/query/pr` |
| `riskLimitUtilization` | REST API address for querying risk limit utilization data | `/api/v1.0/query/rlu` |
| `healthz` | REST API address for find out whether the Store Manager is running | `/healthz` |

### HTTP

The `http` section configures the web based UI and the REST API.

| Option | Explanation | Example |
|--------|-------------|---------|
| `port` | Port of the HTTP(S) server | `8080` |
| `compression` | Enbale gzip compression of the HTTP responses | `true` |
| `ssl` | Subsection configuring SSL/TLS on the webserver |  |
| `CORS` | Subsection configuring Cross-origin resource sharing (see below) |  |
| `CSRF` | Subsection configuring Cross-site request forgery protection (see below) |  |
| `auth` | Subsection configuring authentication (see below) |  |


### SSL

The `ssl` section configures the SSL/TLS support in the webserver.

| Option | Explanation | Example |
|--------|-------------|---------|
| `enable` | Enable HTTPS protocol | `true` |
| `keystore` | JKS file with the private key |  |
| `keystorePassword` | Password to the JKS file containing the private key |  |
| `truststore` | JKS file with trusted client CAs |  |
| `truststorePassword` | Password to the JKS file containing the trusted certificates |  |
| `requireTLSClientAuth` | Sets TLS client authentication as required | `false` |

#### Auth

The `auth` subsection configures authentication to the UI and REST interface. The Mongo databases configured from authentication can be different database from the one configured for storing ERS data.

| Option | Explanation | Example |
|--------|-------------|---------|
| `enable` | Disables or enables authentication | `true` |
| `dbName` | Name of the database which will be used | `DAVe` |
| `connectionUrl` | Connection URL to connect to the database | `mongodb://localhost:27017` |
| `jwtKeystorePath` | Path to the keystore (JCEKS type) required by JWT authentication provider | |
| `jwtKeystorePassword`| Password to the JWT keystore (JCEKS type) used by authentication provider | |
| `jwtTokenExpiration`| Expiration time (in minutes) when the JWT token expires | `60` |
| `salt` | Salt string used in hashed passwords | `sdf8hdgss3_a` |
| `checkUserAgainstCertificate` | Validate username against the CN from the TLS client certificate subject. Unless the CN is equal to the username, authentication will be refused. _*)_ | `false` |

_*) This feature doesn't work properly with self-signed certificates, where the holder of the certificate can easily issue and sign another certificate which would contain different CN and login._

#### CORS

The `CORS` subsection configures Cross-origin resource sharing (CORS), which allows the REST API to be used from web applications running under different domain.

| Option | Explanation | Example |
|--------|-------------|---------|
| `enable` | Enabled the CORS handler | `true` |
| `origin` | Configures the domain from which cross-origin access will be allowed | `http://mydomain.com` |

#### CSRF

The `CSRF` subsection configures Cross-site request forgery (CSRF) protection. When enabled, the handler will set a XSRF-TOKEN cookie and the client has to send back its value in the X-XSRF-TOKEN header. This handler weill be activated only when authentication is enabled.

| Option | Explanation | Example |
|--------|-------------|---------|
| `enable` | Enable the CSRF handler | `true` |
| `secret` | Configures the domain secret used to generate CSFR tokens | `61d77a85-276b-476a-8810-f8408b5cfa19` |

## Run

Use script `start_dave.sh|bat` to start the application depending on your operating system (Linux,MacOS | Windows).

## Managing user database

Use script `user_manager.sh`. Script accepts one of the following commands:
  - `insert`
  - `delete`
  - `list`

### Insert new user record
      user_manager.sh insert USER PASSWORD

### Delete existing user record
      user_manager.sh delete USER

### List all user records
      user_manager.sh list
