#!/bin/bash

WHEREAMI=`dirname "${0}"`
if [ -z "${OPNFI_RISK_ROOT}" ]; then
    export OPNFI_RISK_ROOT=`cd "${WHEREAMI}/../" && pwd`
fi

OPNFI_RISK_LIB=${OPNFI_RISK_ROOT}/lib
OPNFI_RISK_ETC=${OPNFI_RISK_ROOT}/etc

CMD=$1
OPTIONS="-Dcmd=${CMD}"

case "$CMD" in
  "insert") OPTIONS="${OPTIONS} -DuserName=$2 -DuserPassword=$3" ;;
  "list") ;;
  "delete") OPTIONS="${OPTIONS} -DuserName=$2" ;;
  *) echo "Usage ${0} CMD OPTIONS (where CMD must be in: [insert, delete, list])"
     exit -1 ;;
esac


java \
  -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory \
  ${OPTIONS} \
  -cp ${OPNFI_RISK_LIB}/risk-1.0-SNAPSHOT-fat.jar com.opnfi.risk.util.UserManagerVerticle \
  -conf ${OPNFI_RISK_ETC}/opnfi-risk.json
