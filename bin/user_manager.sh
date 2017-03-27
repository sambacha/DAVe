#!/bin/bash

WHEREAMI=`dirname "${0}"`
if [ -z "${DAVE_ROOT}" ]; then
    export DAVE_ROOT=`cd "${WHEREAMI}/../" && pwd`
fi

DAVE_LIB=${DAVE_ROOT}/lib
DAVE_ETC=${DAVE_ROOT}/etc

CMD=$1
OPTIONS="-Dcmd=${CMD}"

case "$CMD" in
  "insert") OPTIONS="${OPTIONS} -DuserName=$2 -DuserPassword=$3" ;;
  "list") ;;
  "delete") OPTIONS="${OPTIONS} -DuserName=$2" ;;
  *) echo "Usage ${0} CMD OPTIONS (where CMD must be in: [insert, delete, list])"
     exit -1 ;;
esac


java ${JAVA_OPTS} \
  -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory \
  ${OPTIONS} \
  -cp ${DAVE_LIB}/dave-1.0-SNAPSHOT-fat.jar com.deutscheboerse.risk.dave.util.UserManagerVerticle \
  -conf ${DAVE_ETC}/dave.json
