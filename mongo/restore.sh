#!/bin/bash

MONGO_DB=DAVe-TTSave
DUMP_DIR=mongo/DAVe-TTSave

mongorestore -c "ers.TradingSessionStatus" -d ${MONGO_DB} ${DUMP_DIR}/ers.TradingSessionStatus.bson
mongorestore -c "ers.TradingSessionStatus.latest" -d ${MONGO_DB} ${DUMP_DIR}/ers.TradingSessionStatus.latest.bson
mongorestore -c "ers.MarginComponent" -d ${MONGO_DB} ${DUMP_DIR}/ers.MarginComponent.bson
mongorestore -c "ers.MarginComponent.latest" -d ${MONGO_DB} ${DUMP_DIR}/ers.MarginComponent.latest.bson
mongorestore -c "ers.TotalMarginRequirement" -d ${MONGO_DB} ${DUMP_DIR}/ers.TotalMarginRequirement.bson
mongorestore -c "ers.TotalMarginRequirement.latest" -d ${MONGO_DB} ${DUMP_DIR}/ers.TotalMarginRequirement.latest.bson
mongorestore -c "ers.MarginShortfallSurplus" -d ${MONGO_DB} ${DUMP_DIR}/ers.MarginShortfallSurplus.bson
mongorestore -c "ers.MarginShortfallSurplus.latest" -d ${MONGO_DB} ${DUMP_DIR}/ers.MarginShortfallSurplus.latest.bson
mongorestore -c "ers.PositionReport" -d ${MONGO_DB} ${DUMP_DIR}/ers.PositionReport.bson
mongorestore -c "ers.PositionReport.latest" -d ${MONGO_DB} ${DUMP_DIR}/ers.PositionReport.latest.bson
mongorestore -c "ers.RiskLimit" -d ${MONGO_DB} ${DUMP_DIR}/ers.RiskLimit.bson
mongorestore -c "ers.RiskLimit.latest" -d ${MONGO_DB} ${DUMP_DIR}/ers.RiskLimit.latest.bson
mongorestore -c "user" -d ${MONGO_DB} ${DUMP_DIR}/user.bson

