#!/bin/bash
set -e

# if command starts with an option, prepend the start script
if [ "${1:0:1}" = '-' ]; then
  set -- ./bin/start_dave.sh "$@"
fi

if [ "$1" = "./bin/start_dave.sh" ]; then
  CONFIG_DB=()
  CONFIG_HTTP=()
  CONFIG_ERS_DEBUGGER=()
  CONFIG_ERS=()
  CONFIG_MASTERDATA=()

  #####
  # DB
  #####
    # DB name
    if [ -z "$DAVE_DB_NAME" ]; then
      DAVE_DB_NAME="DAVe"
    fi

    # DB hostname
    if [ -z "$DAVE_DB_HOSTNAME" ]; then
      DAVE_DB_HOSTNAME="mongo"
    fi

    # DB port
    if [ -z "$DAVE_DB_PORT" ]; then
      DAVE_DB_PORT=27017
    fi

    # DB username
    if [ -z "$DAVE_DB_USERNAME" ]; then
      DAVE_DB_USERNAME=""
    fi

    # DB password
    if [ -z "$DAVE_DB_PASSWORD" ]; then
      DAVE_DB_PASSWORD=""
    fi

    # DB URL
    if [[ "$DAVE_DB_USERNAME" && "$DAVE_DB_PASSWORD" ]]; then
      DAVE_DB_URL="mongodb://${DAVE_DB_USERNAME}:${DAVE_DB_PASSWORD}@${DAVE_DB_HOSTNAME}:${DAVE_DB_PORT}"
    else
      DAVE_DB_URL="mongodb://${DAVE_DB_HOSTNAME}:${DAVE_DB_PORT}"
    fi

    db_config="\"db_name\": \"${DAVE_DB_NAME}\", \"connection_string\": \"${DAVE_DB_URL}\""
    CONFIG_DB+=("$db_config")

  #####
  # HTTP
  #####
  if [ "$DAVE_HTTP_ENABLE" ]; then
    # Compression
    if [ "$DAVE_HTTP_COMPRESSION" ]; then
      http_compression="\"compression\": true"
      CONFIG_HTTP+=("$http_compression")
    else
      http_compression="\"compression\": false"
      CONFIG_HTTP+=("$http_compression")
    fi

    # CORS
    if [ "$DAVE_HTTP_CORS" ]; then
      if [ -z "$DAVE_HTTP_CORS_ORIGIN" ]; then
        DAVE_HTTP_CORS_ORIGIN="*"
      fi
      http_cors="\"CORS\": { \"enable\": true, \"origin\": \"${DAVE_HTTP_CORS_ORIGIN}\" }"
      CONFIG_HTTP+=("$http_cors")
    else
      http_cors="\"CORS\": { \"enable\": false }"
      CONFIG_HTTP+=("$http_cors")
    fi

    # CORS
    if [ "$DAVE_HTTP_CSRF" ]; then
      if [ -z "$DAVE_HTTP_CSRF_SECRET" ]; then
        DAVE_HTTP_CSRF_SECRET="$(date +%s | sha256sum | base64 | head -c 32 ; echo)"
      fi
      http_csrf="\"CSRF\": { \"enable\": true, \"secret\": \"${DAVE_HTTP_CSRF_SECRET}\" }"
      CONFIG_HTTP+=("$http_csrf")
    else
      http_csrf="\"CSRF\": { \"enable\": false }"
      CONFIG_HTTP+=("$http_csrf")
    fi

    # SSL
    if [[ "$DAVE_HTTP_SSL_SERVER_PUBLIC_KEY" && "$DAVE_HTTP_SSL_SERVER_PRIVATE_KEY" ]]; then
      jks_password="$(date +%s | sha256sum | base64 | head -c 32 ; echo)"

      keystorePath="$(pwd)/etc/http.keystore"
      tempDir="$(mktemp -d)"
      echo "$DAVE_HTTP_SSL_SERVER_PUBLIC_KEY" > $tempDir/keystore.crt
      echo "$DAVE_HTTP_SSL_SERVER_PRIVATE_KEY" > $tempDir/keystore.pem
      openssl pkcs12 -export -in $tempDir/keystore.crt -inkey $tempDir/keystore.pem -out $tempDir/keystore.p12 -passout pass:$jks_password
      keytool -importkeystore -srckeystore $tempDir/keystore.p12 -srcstoretype PKCS12 -srcstorepass $jks_password -destkeystore $keystorePath -deststoretype JKS -deststorepass $jks_password
      rm -rf $tempDir

      if [ "$DAVE_HTTP_SSL_TRUSTED_CA" ]; then
        truststorePath="$(pwd)/etc/http.truststore"
        tempDir="$(mktemp -d)"

        pushd $tempDir
        echo "$DAVE_HTTP_SSL_TRUSTED_CA" > db.ca
        csplit db.ca '/^-----END CERTIFICATE-----$/1' '{*}' --elide-empty-files --silent --prefix=ca_

        counter=1
        for cert in $(ls ca_*); do
          keytool -importcert -noprompt -keystore $truststorePath -keypass $jks_password -storetype JKS -file $cert -alias "ca_${counter}"
          let counter=$counter+1
        done

        rm ca_*
        rm db.ca
        popd

        if [ "$DAVE_HTTP_SSL_CLIENT_AUTH" ]; then
          ssl_client_auth=true
        else
          ssl_client_auth=false
        fi

        http_ssl="\"ssl\": { \"enable\": true, \"keystore\": \"${keystorePath}\", \"keystorePassword\": \"${jks_password}\", \"truststore\": \"${truststorePath}\", \"truststorePassword\": \"${jks_password}\", \"requireTLSClientAuth\": ${ssl_client_auth} }"
      else
        http_ssl="\"ssl\": { \"enable\": true, \"keystore\": \"${keystorePath}\", \"keystorePassword\": \"${jks_password}\" }"
      fi

      CONFIG_HTTP+=("$http_ssl")
    fi

    # AUTH
    if [ "$DAVE_HTTP_AUTH" ]; then
      if [ -z "$DAVE_HTTP_AUTH_SALT" ]; then
        DAVE_HTTP_AUTH_SALT="DAVe"
      fi

      if [ -z "$DAVE_HTTP_AUTH_LINK_SSL" ]; then
        DAVE_HTTP_AUTH_LINK_SSL="false"
      fi

      http_auth="\"auth\": { \"enable\": true, \"salt\": \"${DAVE_HTTP_AUTH_SALT}\", \"db_name\": \"${DAVE_DB_NAME}\", \"connection_string\": \"${DAVE_DB_URL}\", \"checkUserAgainstCertificate\": ${DAVE_HTTP_AUTH_LINK_SSL} }"
      CONFIG_HTTP+=("$http_auth")
    fi
  fi

  #####
  # ERS Debugger
  #####
  if [ "$DAVE_ERS_DEBUGGER" ]; then
    dbg_config="\"enable\": true"
    CONFIG_ERS_DEBUGGER+=("$dbg_config")
  else
    dbg_config="\"enable\": false"
    CONFIG_ERS_DEBUGGER+=("$dbg_config")
  fi

  #####
  ## Write the config file
  #####
  configFile="$(pwd)/etc/dave.json"
  cat >> $configFile <<-EOS
{
EOS
  IFSBAK="$IFS"
  IFS=", "
  cat >> $configFile <<-EOS
  "http": {
    ${CONFIG_HTTP[*]}
  },
  "mongodb": {
    ${CONFIG_DB[*]}
  },
  "ersDebugger": {
    ${CONFIG_ERS_DEBUGGER[*]}
  },
EOS
  IFS="$IFSBAK"
  cat >> $configFile <<-EOS
  "ers": []
}
EOS

  #chown -R dave:dave /home/dave
fi

# else default to run whatever the user wanted like "bash"
exec "$@"
