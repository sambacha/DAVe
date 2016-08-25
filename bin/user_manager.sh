#!/bin/bash
#
# adds user/password to the database
# param1 - user
# param2 - password

WHEREAMI=`dirname "${0}"`
if [ -z "${OPNFI_RISK_ROOT}" ]; then
    export OPNFI_RISK_ROOT=`cd "${WHEREAMI}/../" && pwd`
fi

OPNFI_RISK_LIB=${OPNFI_RISK_ROOT}/lib
OPNFI_RISK_ETC=${OPNFI_RISK_ROOT}/etc

java \
  -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory \
  -DuserName="$1" \
  -DuserPassword="$2" \
  -cp ${OPNFI_RISK_LIB}/risk-1.0-SNAPSHOT-fat.jar io.vertx.core.Launcher \
  run com.opnfi.risk.util.UserManagerVerticle -conf ${OPNFI_RISK_ETC}/opnfi-risk.json
