[![CircleCI](https://circleci.com/gh/Deutsche-Boerse-Risk/DAVe.svg?style=shield)](https://circleci.com/gh/Deutsche-Boerse-Risk/DAVe) [![Build Status](https://travis-ci.org/Deutsche-Boerse-Risk/DAVe.svg?branch=master)](https://travis-ci.org/Deutsche-Boerse-Risk/DAVe) [![Coverage Status](https://coveralls.io/repos/github/Deutsche-Boerse-Risk/DAVe/badge.svg?branch=master)](https://coveralls.io/github/Deutsche-Boerse-Risk/DAVe?branch=master) [![codebeat badge](https://codebeat.co/badges/54fe7c25-2329-4b95-8172-f80a01611474)](https://codebeat.co/projects/github-com-deutsche-boerse-risk-dave) [![Dependency Status](https://dependencyci.com/github/Deutsche-Boerse-Risk/DAVe/badge)](https://dependencyci.com/github/Deutsche-Boerse-Risk/DAVe)

# DAVe

**DAVe** is **D**ata **A**nalytics and **V**isualisation S**e**rvice. It is free open source client which connects to [Eurex Clearing Enhanced Risk Interface](http://www.eurexclearing.com/clearing-en/risk-management/system-based-risk-controls/post-trade-risk-control/enhanced-risk-interface) AMQP interface (ERS). It provides a UI and REST interface to access latest as well as historical data received over ERS.

![DAVe - Dashboard](https://github.com/Deutsche-Boerse-Risk/DAVe/blob/master/doc/screenshots/dave-screenshots.gif "DAVe - Dashboard")

## Build
    mvn clean package

The shippable artifact will be built in `target/dave-VERSION` directory.

## Configure

Configuration is stored in `dave.json` file in JSON format. Configuration is split into several sections:

### ERS

The `ers` section configures the Enhanced Risk Solution connection. It is an array which might contain several JSON objects - each object representing one ERS connection.

| Option | Explanation | Example |
|--------|-------------|---------|
| `brokerHost` | ERS broker hostname | `localhost.de` |
| `brokerPort` | ERS broker port | `18080` |
| `member` | Member ID which will be used for the ERS connection | `ABCFR` |
| `truststore` | JKS file with the public key of the ERS broker |  |
| `truststorePassword` | Password to the JKS file containing the public key |  |
| `keystore` | JKS file with the private key |  |
| `keystorePassword` | Password to the JKS file containing the private key |  |
| `sslCertAlias` | Alias of the private key which should be used for the connection |  |
| `connectionPoolSize` | Defines the pool size of the connection factory | `10` |

### MongoDB

The `mongodb` section contains the configuration of the MongoDB database where will the ERS data be persisted.

| Option | Explanation | Example |
|--------|-------------|---------|
| `db_name` | Name of the database which will be used | `DAVe` |
| `connection_string` | Connection URL to connect to the database | `mongodb://localhost:27017` |

### HTTP

The `http` section configures the web based UI and the REST API.

| Option | Explanation | Example |
|--------|-------------|---------|
| `httpPort` | Port of the HTTP server | `8080` |
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
| `truststore` | Password to the JKS file containing the trusted certificates |  |
| `requireTLSClientAuth` | Sets TLS client authentication as required | `false` |

#### Auth

The `auth` subsection configures authentication to the UI and REST interface. The Mongo databases configured from authentication can be different database from the one configured for storing ERS data.

| Option | Explanation | Example |
|--------|-------------|---------|
| `enable` | Disables or enables authetication | `true` |
| `db_name` | Name of the database which will be used | `DAVe` |
| `connection_string` | Connection URL to connect to the database | `mongodb://localhost:27017` |
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

Use script `start_dave.sh` to start the application. The script works on Linux and MacOS.

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
