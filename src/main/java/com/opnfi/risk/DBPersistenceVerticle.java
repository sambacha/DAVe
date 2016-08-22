package com.opnfi.risk;

import com.opnfi.risk.model.MarginComponent;
import com.opnfi.risk.model.MarginShortfallSurplus;
import com.opnfi.risk.model.TotalMarginRequirement;
import com.opnfi.risk.model.TradingSessionStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by schojak on 19.8.16.
 */
public class DBPersistenceVerticle extends AbstractVerticle {
    final static private Logger LOG = LoggerFactory.getLogger(com.opnfi.risk.DBPersistenceVerticle.class);

    private JDBCClient jdbc;
    final DateFormat timestampFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    @Override
    public void start(Future<Void> fut) throws Exception {
        startDb(
                (connection) -> initDb(
                        connection,
                        (nothing) -> startEventBus(fut),
                        fut),
                fut);
    }

    private void startDb(Handler<AsyncResult<SQLConnection>> next, Future<Void> fut) {
        LOG.info("Connecting to JDBC database");
        jdbc = JDBCClient.createShared(vertx, new JsonObject().put("url", "jdbc:h2:mem:opnfi-risk").put("driver_class", "org.h2.Driver"), "OpnFi-Risk");

        jdbc.getConnection(ar -> {
            if (ar.failed()) {
                fut.fail(ar.cause());
            } else {
                LOG.info("Connected to JDBC database");
                next.handle(Future.succeededFuture(ar.result()));
            }
        });
    }

    /*
    Initialize the database - create the table for messages using SQL
     */
    private void initDb(AsyncResult<SQLConnection> result, Handler<AsyncResult<Void>> next, Future<Void> fut) {
        if (result.failed()) {
            fut.fail(result.cause());
        } else {
            SQLConnection jdbcConnection = result.result();

            List<String> createTables = new LinkedList<String>();
            createTables.add("CREATE TABLE \"margin_component\" ( \"id\" BIGINT AUTO_INCREMENT PRIMARY KEY, \"clearer\" VARCHAR(255), \"member\" VARCHAR(255), \"account\" VARCHAR(255), \"clss\" VARCHAR(255), \"ccy\" VARCHAR(255), \"txnTm\" TIMESTAMP, \"bizDt\" TIMESTAMP, \"reqId\" VARCHAR(255), \"rptId\" VARCHAR(255), \"sesId\" VARCHAR(255), \"variationMargin\" DECIMAL(38), \"premiumMargin\" DECIMAL(38), \"liquiMargin\" DECIMAL(38), \"spreadMargin\" DECIMAL(38), \"additionalMargin\" DECIMAL(38), \"received\" TIMESTAMP);");
            createTables.add("CREATE TABLE \"margin_shortfall_surplus\" ( \"id\" BIGINT AUTO_INCREMENT PRIMARY KEY, \"clearer\" VARCHAR(255), \"pool\" VARCHAR(255), \"poolType\" VARCHAR(255), \"member\" VARCHAR(255), \"clearingCcy\" VARCHAR(255), \"ccy\" VARCHAR(255), \"txnTm\" TIMESTAMP, \"bizDt\" TIMESTAMP, \"reqId\" VARCHAR(255), \"rptId\" VARCHAR(255), \"sesId\" VARCHAR(255), \"marginRequirement\" DECIMAL(38), \"securityCollateral\" DECIMAL(38), \"cashBalance\" DECIMAL(38), \"shortfallSurplus\" DECIMAL(38), \"marginCall\" DECIMAL(38), \"received\" TIMESTAMP);");
            createTables.add("CREATE TABLE \"total_margin_requirement\" ( \"id\" BIGINT AUTO_INCREMENT PRIMARY KEY, \"clearer\" VARCHAR(255), \"pool\" VARCHAR(255), \"member\" VARCHAR(255), \"account\" VARCHAR(255), \"ccy\" VARCHAR(255), \"txnTm\" TIMESTAMP, \"bizDt\" TIMESTAMP, \"reqId\" VARCHAR(255), \"rptId\" VARCHAR(255), \"sesId\" VARCHAR(255), \"unadjustedMargin\" DECIMAL(38), \"adjustedMargin\" DECIMAL(38), \"received\" TIMESTAMP);");
            createTables.add("CREATE TABLE \"trading_session_status\" ( \"id\" BIGINT AUTO_INCREMENT PRIMARY KEY, \"reqId\" VARCHAR(255), \"sesId\" VARCHAR(255), \"stat\" VARCHAR(255), \"statRejRsn\" VARCHAR(255), \"txt\" VARCHAR(255));");
            createTables.add("CREATE VIEW \"margin_component_latest\" AS SELECT t.* FROM (SELECT \"clearer\", \"member\", \"account\", \"clss\", \"ccy\", MAX(\"received\") AS \"received\" FROM \"margin_component\" GROUP BY \"clearer\", \"member\", \"account\", \"clss\", \"ccy\") x JOIN \"margin_component\" t ON x.\"received\" =t.\"received\" AND t.\"clearer\" = x.\"clearer\" AND t.\"member\" = x.\"member\" AND t.\"account\" = x.\"account\" AND t.\"clss\" = x.\"clss\" AND x.\"ccy\" = t.\"ccy\";");
            createTables.add("CREATE VIEW \"total_margin_requirement_latest\" AS SELECT t.* FROM (SELECT \"clearer\", \"pool\", \"member\", \"account\", \"ccy\", MAX(\"received\") AS \"received\" FROM \"total_margin_requirement\" GROUP BY \"clearer\", \"pool\", \"member\", \"account\", \"ccy\") x JOIN \"total_margin_requirement\" t ON x.\"received\" =t.\"received\" AND t.\"clearer\" = x.\"clearer\" AND t.\"pool\" = x.\"pool\" AND t.\"member\" = x.\"member\" AND t.\"account\" = x.\"account\" AND x.\"ccy\" = t.\"ccy\";");
            createTables.add("CREATE VIEW \"margin_shortfall_surplus_latest\" AS SELECT t.* FROM (SELECT \"clearer\", \"pool\", \"member\", \"clearingCcy\", \"ccy\", MAX(\"received\") AS \"received\" FROM \"margin_shortfall_surplus\" GROUP BY \"clearer\", \"pool\", \"member\", \"clearingCcy\", \"ccy\") x JOIN \"margin_shortfall_surplus\" t ON x.\"received\" =t.\"received\" AND t.\"clearer\" = x.\"clearer\" AND t.\"pool\" = x.\"pool\" AND t.\"member\" = x.\"member\" AND t.\"clearingCcy\" = x.\"clearingCcy\" AND x.\"ccy\" = t.\"ccy\";");

            jdbcConnection.batch(
                    createTables,
                    ar -> {
                        if (ar.failed()) {
                            LOG.error("Failed to create DB structure", ar.cause());
                            fut.fail(ar.cause());
                            jdbcConnection.close();
                        }
                        else {
                            LOG.info("DB structure created");
                            next.handle(fut);
                        }
                    });
        }
    }

    private void startEventBus(Future<Void> fut)
    {
        EventBus eb = vertx.eventBus();

        // Camel consumers
        eb.consumer("ers.TradingSessionStatus", message -> storeTradingSessionStatus(message));
        eb.consumer("ers.MarginComponent", message -> storeMarginComponent(message));
        eb.consumer("ers.TotalMarginRequirement", message -> storeTotalMarginRequirement(message));
        eb.consumer("ers.MarginShortfallSurplus", message -> storeMarginShortfallSurplus(message));

        // Query endpoints
        eb.consumer("db.query.MarginComponent", message -> queryMarginComponent(message));
        eb.consumer("db.query.TotalMarginRequirement", message -> queryTotalMarginRequirement(message));
        eb.consumer("db.query.MarginShortfallSurplus", message -> queryMarginShortfallSurplus(message));

        fut.complete();
    }

    private void storeTradingSessionStatus(Message msg)
    {
        LOG.trace("Storing TSS message with body: " + msg.body().toString());
        TradingSessionStatus tss = Json.decodeValue(msg.body().toString(), TradingSessionStatus.class);

        String sql = "INSERT INTO \"trading_session_status\" (\"reqId\", \"sesId\", \"stat\", \"statRejRsn\", \"txt\") VALUES (?, ?, ?, ?, ?)";
        JsonArray params = new JsonArray()
                .add(tss.getReqId() != null ? tss.getReqId() : "null")
                .add(tss.getSesId() != null ? tss.getSesId() : "null")
                .add(tss.getStat() != null ? tss.getStat() : "null")
                .add(tss.getStatRejRsn() != null ? tss.getStatRejRsn() : "null")
                .add(tss.getTxt() != null ? tss.getTxt() : "null");

        jdbc.getConnection(ar -> {
            SQLConnection connection = ar.result();
            connection.updateWithParams(sql,
                    params,
                    (ar2) -> {
                        if (ar2.failed()) {
                            LOG.error("Failed to store TradingSessionStatus into DB " + ar2.cause());
                        }
                        LOG.trace("Stored TradingSessionStatus into DB");
                    });
            connection.close();
        });
    }

    private void storeMarginComponent(Message msg)
    {
        LOG.trace("Storing MC message with body: " + msg.body().toString());
        MarginComponent mc = Json.decodeValue(msg.body().toString(), MarginComponent.class);

        String sql = "INSERT INTO \"margin_component\" (\"clearer\", \"member\", \"account\", \"clss\", \"ccy\", \"txnTm\", \"bizDt\", \"reqId\", \"rptId\", \"sesId\", \"variationMargin\", \"premiumMargin\", \"liquiMargin\", \"spreadMargin\", \"additionalMargin\", \"received\") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        JsonArray params = new JsonArray()
                .add(mc.getClearer() != null ? mc.getClearer() : "null")
                .add(mc.getMember() != null ? mc.getMember() : "null")
                .add(mc.getAccount() != null ? mc.getAccount() : "null")
                .add(mc.getClss() != null ? mc.getClss() : "null")
                .add(mc.getCcy() != null ? mc.getCcy() : "null")
                .add(mc.getTxnTm() != null ? timestampFormatter.format(mc.getTxnTm()) : "null")
                .add(mc.getBizDt() != null ? timestampFormatter.format(mc.getBizDt()) : "null")
                .add(mc.getReqId() != null ? mc.getReqId() : "null")
                .add(mc.getRptId() != null ? mc.getRptId() : "null")
                .add(mc.getSesId() != null ? mc.getSesId() : "null")
                .add(mc.getVariationMargin() != null ? mc.getVariationMargin().toString() : "null")
                .add(mc.getPremiumMargin() != null ? mc.getPremiumMargin().toString() : "null")
                .add(mc.getLiquiMargin() != null ? mc.getLiquiMargin().toString() : "null")
                .add(mc.getSpreadMargin() != null ? mc.getSpreadMargin().toString() : "null")
                .add(mc.getVariationMargin() != null ? mc.getVariationMargin().toString() : "null")
                .add(mc.getReceived() != null ? timestampFormatter.format(mc.getReceived()) : "null");

        jdbc.getConnection(ar -> {
            SQLConnection connection = ar.result();
            connection.updateWithParams(sql,
                    params,
                    (ar2) -> {
                        if (ar2.failed()) {
                            LOG.error("Failed to store MarginComponent into DB " + ar2.cause());
                        }
                        LOG.trace("Stored MarginComponent into DB");
                    });
            connection.close();
        });
    }

    private void storeTotalMarginRequirement(Message msg)
    {
        LOG.trace("Storing TMR message with body: " + msg.body().toString());
        TotalMarginRequirement tmr = Json.decodeValue(msg.body().toString(), TotalMarginRequirement.class);

        String sql = "INSERT INTO \"total_margin_requirement\" (\"clearer\", \"pool\", \"member\", \"account\", \"ccy\", \"txnTm\", \"bizDt\", \"reqId\", \"rptId\", \"sesId\", \"unadjustedMargin\", \"adjustedMargin\", \"received\") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        JsonArray params = new JsonArray()
                .add(tmr.getClearer() != null ? tmr.getClearer() : "null")
                .add(tmr.getPool() != null ? tmr.getPool() : "null")
                .add(tmr.getMember() != null ? tmr.getMember() : "null")
                .add(tmr.getAccount() != null ? tmr.getAccount() : "null")
                .add(tmr.getCcy() != null ? tmr.getCcy() : "null")
                .add(tmr.getTxnTm() != null ? timestampFormatter.format(tmr.getTxnTm()) : "null")
                .add(tmr.getBizDt() != null ? timestampFormatter.format(tmr.getBizDt()) : "null")
                .add(tmr.getReqId() != null ? tmr.getReqId() : "null")
                .add(tmr.getRptId() != null ? tmr.getRptId() : "null")
                .add(tmr.getSesId() != null ? tmr.getSesId() : "null")
                .add(tmr.getUnadjustedMargin() != null ? tmr.getUnadjustedMargin().toString() : "null")
                .add(tmr.getAdjustedMargin() != null ? tmr.getAdjustedMargin().toString() : "null")
                .add(tmr.getReceived() != null ? timestampFormatter.format(tmr.getReceived()) : "null");

        jdbc.getConnection(ar -> {
            SQLConnection connection = ar.result();
            connection.updateWithParams(sql,
                    params,
                    (ar2) -> {
                        if (ar2.failed()) {
                            LOG.error("Failed to store TotalMarginRequirement into DB " + ar2.cause());
                        }
                        LOG.trace("Stored TotalMarginRequirement into DB");
                    });

            connection.close();
        });
    }

    private void storeMarginShortfallSurplus(Message msg)
    {
        LOG.trace("Storing MSS message with body: " + msg.body().toString());
        MarginShortfallSurplus mss = Json.decodeValue(msg.body().toString(), MarginShortfallSurplus.class);

        String sql = "INSERT INTO \"margin_shortfall_surplus\" (\"clearer\", \"pool\", \"poolType\", \"member\", \"clearingCcy\", \"ccy\", \"txnTm\", \"bizDt\", \"reqId\", \"rptId\", \"sesId\", \"marginRequirement\", \"securityCollateral\", \"cashBalance\", \"shortfallSurplus\", \"marginCall\", \"received\") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        JsonArray params = new JsonArray()
                .add(mss.getClearer() != null ? mss.getClearer() : "null")
                .add(mss.getPool() != null ? mss.getPool() : "null")
                .add(mss.getPoolType() != null ? mss.getPoolType() : "null")
                .add(mss.getMember() != null ? mss.getMember() : "null")
                .add(mss.getClearingCcy() != null ? mss.getClearingCcy() : "null")
                .add(mss.getCcy() != null ? mss.getCcy() : "null")
                .add(mss.getTxnTm() != null ? timestampFormatter.format(mss.getTxnTm()) : "null")
                .add(mss.getBizDt() != null ? timestampFormatter.format(mss.getBizDt()) : "null")
                .add(mss.getReqId() != null ? mss.getReqId() : "null")
                .add(mss.getRptId() != null ? mss.getRptId() : "null")
                .add(mss.getSesId() != null ? mss.getSesId() : "null")
                .add(mss.getMarginRequirement() != null ? mss.getMarginRequirement().toString() : "null")
                .add(mss.getSecurityCollateral() != null ? mss.getSecurityCollateral().toString() : "null")
                .add(mss.getCashBalance() != null ? mss.getCashBalance().toString() : "null")
                .add(mss.getShortfallSurplus() != null ? mss.getShortfallSurplus().toString() : "null")
                .add(mss.getMarginCall() != null ? mss.getMarginCall().toString() : "null")
                .add(mss.getReceived() != null ? timestampFormatter.format(mss.getReceived()) : "null");

        jdbc.getConnection(ar -> {
            SQLConnection connection = ar.result();
            connection.updateWithParams(sql,
                    params,
                    (ar2) -> {
                        if (ar2.failed()) {
                            LOG.error("Failed to store MarginShortfallSurplus into DB " + ar2.cause());
                        }
                        LOG.trace("Stored MarginShortfallSurplus into DB");
                    });

            connection.close();
        });
    }

    private void queryMarginComponent(Message msg)
    {
        JsonArray params = (JsonArray)msg.body();
        LOG.trace("Received latest/mc query with parameters " + params);

        String select = "SELECT \"id\", \"clearer\", \"member\", \"account\", \"clss\", \"ccy\", \"txnTm\", \"bizDt\", \"reqId\", \"rptId\", \"sesId\", \"variationMargin\", \"premiumMargin\", \"liquiMargin\", \"spreadMargin\", \"additionalMargin\", \"received\" FROM \"margin_component_latest\"";
        String where = "";

        if (params.size() > 0)
        {
            for (int i = 0; i < params.size(); ) {
                if (i == 0)
                {
                    where = where + " WHERE";
                }
                else
                {
                    where = where + " AND";
                }

                where = where + " \"" + params.getString(i) + "\"='" + params.getString(i+1) + "'";
                i = i+2;
            }
        }

        String sql = select + where;

        jdbc.getConnection(ar -> {
            if (ar.succeeded()) {
                LOG.trace("Querying database for latest/mc " + sql);
                SQLConnection connection = ar.result();

                connection.query(sql, result -> {
                    if (result.succeeded()) {
                        msg.reply(Json.encodePrettily(result.result().getRows()));
                    } else {
                        LOG.error("latest/mc query failed", result.cause());
                    }
                });

                connection.close();
            } else {
                LOG.error("Failed to obtain database connection", ar.cause());
            }
        });
    }

    private void queryTotalMarginRequirement(Message msg)
    {
        JsonArray params = (JsonArray)msg.body();
        LOG.trace("Received latest/tmr query with parameters " + params);

        String select = "SELECT \"id\", \"clearer\", \"pool\", \"member\", \"account\", \"ccy\", \"txnTm\", \"bizDt\", \"reqId\", \"rptId\", \"sesId\", \"unadjustedMargin\", \"adjustedMargin\", \"received\" FROM \"total_margin_requirement_latest\"";
        String where = "";

        if (params.size() > 0)
        {
            for (int i = 0; i < params.size(); ) {
                if (i == 0)
                {
                    where = where + " WHERE";
                }
                else
                {
                    where = where + " AND";
                }

                where = where + " \"" + params.getString(i) + "\"='" + params.getString(i+1) + "'";
                i = i+2;
            }
        }

        String sql = select + where;

        jdbc.getConnection(ar -> {
            if (ar.succeeded()) {
                LOG.trace("Querying database for latest/tmr " + sql);
                SQLConnection connection = ar.result();

                connection.query(sql, result -> {
                    if (result.succeeded()) {
                        msg.reply(Json.encodePrettily(result.result().getRows()));
                    } else {
                        LOG.error("latest/tmr query failed", result.cause());
                    }
                });

                connection.close();
            } else {
                LOG.error("Failed to obtain database connection", ar.cause());
            }
        });
    }

    private void queryMarginShortfallSurplus(Message msg)
    {
        JsonArray params = (JsonArray)msg.body();
        LOG.trace("Received latest/mss query with parameters " + params);

        String select = "SELECT \"id\", \"clearer\", \"pool\", \"poolType\", \"member\", \"clearingCcy\", \"ccy\", \"txnTm\", \"bizDt\", \"reqId\", \"rptId\", \"sesId\", \"marginRequirement\", \"securityCollateral\", \"cashBalance\", \"shortfallSurplus\", \"marginCall\", \"received\" FROM \"margin_shortfall_surplus_latest\"";
        String where = "";

        if (params.size() > 0)
        {
            for (int i = 0; i < params.size(); ) {
                if (i == 0)
                {
                    where = where + " WHERE";
                }
                else
                {
                    where = where + " AND";
                }

                where = where + " \"" + params.getString(i) + "\"='" + params.getString(i+1) + "'";
                i = i+2;
            }
        }

        String sql = select + where;

        jdbc.getConnection(ar -> {
            if (ar.succeeded()) {
                LOG.trace("Querying database for latest/mss " + sql);
                SQLConnection connection = ar.result();

                connection.query(sql, result -> {
                    if (result.succeeded()) {
                        msg.reply(Json.encodePrettily(result.result().getRows()));
                    } else {
                        LOG.error("latest/mss query failed", result.cause());
                    }
                });

                connection.close();
            } else {
                LOG.error("Failed to obtain database connection", ar.cause());
            }
        });
    }

    @Override
    public void stop() throws Exception {
        jdbc.close();
    }
}
