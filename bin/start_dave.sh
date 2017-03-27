#!/bin/bash

WHEREAMI=`dirname "${0}"`
if [ -z "${DAVE_ROOT}" ]; then
    export DAVE_ROOT=`cd "${WHEREAMI}/../" && pwd`
fi

DAVE_LIB=${DAVE_ROOT}/lib
DAVE_ETC=${DAVE_ROOT}/etc
export DAVE_LOG=${DAVE_ROOT}/log

java ${JAVA_OPTS} -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory \
     -Dlogback.configurationFile=${DAVE_ETC}/logback.xml \
     -jar ${DAVE_LIB}/dave-1.0-SNAPSHOT-fat.jar -conf ${DAVE_ETC}/dave.json
