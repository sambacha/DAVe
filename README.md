# OpnFi Risk

OpnFi Risk is free open source client for [Eurex Clearing Enhanced Risk Interface](http://www.eurexclearing.com/clearing-en/risk-management/system-based-risk-controls/post-trade-risk-control/enhanced-risk-interface) interface AMQP interface (ERS). It provides a UI and REST interface to acces lastest as well as historical data data received over ERS.

## Build
    mvn clean package
    
The shippable artifact will be built in `target/risk-VERSION` directory.

## Configure

Configuration is stored in `opnfi-risk.json` file in JSON format. Configuration is split into several sections:

### ERS

The `ers` section configures the Enhanced Risk Solution connection:

| Option | Explanation | Example |
|--------|-------------|---------|
| `brokerHost` | ERS broker hostname | `tcp://localhost.de` |
| `brokerPort` | ERS broker port | `18080` |
| `member` | Member ID which will be used for the ERS connection | `ABCFR` |
| `truststore` | JKS file with the public key of the ERS broker |  |
| `truststorePassword` | Password to the JKS file containing the public key |  |
| `keystore` | JKS file with the private key |  |
| `keystorePassword` | Password to the JKS file containing the private key |  |
| `sslCertAlias` | Alias of the private key which should be used for the connection |  |

### MongoDB

The `mongodb` section contains the configuration of the MongoDB database where will the ERS data be persisted. 

| Option | Explanation | Example |
|--------|-------------|---------|
| `db_name` | Name of the database which will be used | `OpnFi-Risk` |
| `connection_string` | Connection URL to connect to the database | `mongodb://localhost:27017` |
| `salt` | Salt string used in hashed passwords | `sdf8hdgss3_a` |

### Web

The `web` section configures the web based UI and the REST API.

| Option | Explanation | Example |
|--------|-------------|---------|
| `httpPort` | Port of the HTTP server | `8080` |
| `ssl` | Enable HTTPS protocol |  |
| `keystore` | JKS file with the private key |  |
| `keystorePassword` | Password to the JKS file containing the private key |  |
| `CORS` | Subsection configuring Cross-origin resource sharing (see below) |  |
| `auth` | Subsection cofngiuring authentication (see below) |  |
| `compression` | Enbale gzip compression of the HTTP responses | `true` |

#### Auth

The `auth` subsection configures authentication to the UI and REST interface. The Mongo databases configured from authentication can be different database from the one configured for storing ERS data.

| Option | Explanation | Example |
|--------|-------------|---------|
| `auth` | Disables or enables authetication | `true` |
| `db_name` | Name of the database which will be used | `OpnFi-Risk` |
| `connection_string` | Connection URL to connect to the database | `mongodb://localhost:27017` |
| `salt` | Salt string used in hashed passwords | `sdf8hdgss3_a` |

#### CORS

The `CORS` subsection configures Cross-origin resource sharing (CORS), which allows the REST API to be used from web applications running under different domain. 

| Option | Explanation | Example |
|--------|-------------|---------|
| `allow` | Enabled the CORS handler | `true` |
| `origin` | Configures the domain from which cross-origin access will be allowed | `http://mydomain.com` |

## Run

Use script `start_opnfi_risk.sh` to start the application. The scirpt works on Linux and MacOS. 

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
