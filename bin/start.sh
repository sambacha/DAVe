#!/bin/bash

#DEBUG="-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n"

WHEREAMI=`dirname "${0}"`
if [ -z "${DAVE_ROOT}" ]; then
    export DAVE_ROOT=`cd "${WHEREAMI}/../" && pwd`
fi

export DAVE_LOG_LEVEL="${DAVE_LOG_LEVEL:-info}"

DAVE_LIB=${DAVE_ROOT}/lib
DAVE_ETC=${DAVE_ROOT}/etc

java ${JAVA_OPTS} ${DEBUG} \
     -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory \
     -Dlogback.configurationFile=${DAVE_ETC}/logback.xml \
     -Ddave.configurationFile=${STOREMANAGER_ETC}/dave.conf \
     -jar ${DAVE_LIB}/dave-1.0-SNAPSHOT-fat.jar
