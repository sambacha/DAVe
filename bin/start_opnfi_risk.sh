#!/bin/bash

MY_FILE=$(readlink -f ${BASH_SOURCE[0]})
MY_PATH=$(dirname ${MY_FILE})

OPNFI_RISK_ROOT=$(dirname ${MY_PATH})
OPNFI_RISK_LIB=${OPNFI_RISK_ROOT}/lib
OPNFI_RISK_ETC=${OPNFI_RISK_ROOT}/etc

java -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory -jar ${OPNFI_RISK_LIB}/risk-1.0-SNAPSHOT-fat.jar -conf ${OPNFI_RISK_ETC}/opnfi-risk.json
