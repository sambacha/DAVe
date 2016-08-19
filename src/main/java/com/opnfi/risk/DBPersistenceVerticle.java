package com.opnfi.risk;

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

import java.util.LinkedList;
import java.util.List;

/**
 * Created by schojak on 19.8.16.
 */
public class DBPersistenceVerticle extends AbstractVerticle {
    final static private Logger LOG = LoggerFactory.getLogger(com.opnfi.risk.DBPersistenceVerticle.class);

    private JDBCClient jdbc;

    @Override
    public void start(Future<Void> fut) throws Exception {
        startDb(
                (connection) -> initDb(
                        connection,
                        (nothing) -> startPersisting(fut),
                        fut),
                fut);
    }

    private void startDb(Handler<AsyncResult<SQLConnection>> next, Future<Void> fut) {
        LOG.info("Connecting to JDBC database");
        jdbc = JDBCClient.createShared(vertx, new JsonObject().put("url", "jdbc:hsqldb:mem:db/code-examples").put("driver_class", "org.hsqldb.jdbcDriver"), "OpnFi-Risk");

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
            createTables.add("CREATE TABLE margin_component ( id bigint NOT NULL, clearer VARCHAR(255), member VARCHAR(255), account VARCHAR(255), clss VARCHAR(255), ccy VARCHAR(255), txn_tm TIMESTAMP, biz_dt TIMESTAMP, req_id VARCHAR(255), rpt_id VARCHAR(255), ses_id VARCHAR(255), variation_margin DECIMAL(38), premium_margin DECIMAL(38), liqui_margin DECIMAL(38), spread_margin DECIMAL(38), additional_margin DECIMAL(38), received TIMESTAMP, CONSTRAINT pk_margin_component PRIMARY KEY (id));");
            createTables.add("CREATE TABLE margin_shortfall_surplus ( id bigint NOT NULL, clearer VARCHAR(255), pool VARCHAR(255), pool_type VARCHAR(255), member VARCHAR(255), clearing_ccy VARCHAR(255), ccy VARCHAR(255), txn_tm TIMESTAMP, biz_dt TIMESTAMP, req_id VARCHAR(255), rpt_id VARCHAR(255), ses_id VARCHAR(255), margin_requirement DECIMAL(38), security_collateral DECIMAL(38), cash_balance DECIMAL(38), shortfall_surplus DECIMAL(38), margin_call DECIMAL(38), received TIMESTAMP, CONSTRAINT pk_margin_shortfall_surplus PRIMARY KEY (id));");
            createTables.add("CREATE TABLE total_margin_requirement ( id bigint NOT NULL, clearer VARCHAR(255), pool VARCHAR(255), member VARCHAR(255), account VARCHAR(255), ccy VARCHAR(255), txn_tm TIMESTAMP, biz_dt TIMESTAMP, req_id VARCHAR(255), rpt_id VARCHAR(255), ses_id VARCHAR(255), unadjusted_margin DECIMAL(38), adjusted_margin DECIMAL(38), received TIMESTAMP, CONSTRAINT pk_total_margin_requirement PRIMARY KEY (id));");
            createTables.add("CREATE TABLE trading_session_status ( id bigint NOT NULL, req_id VARCHAR(255), ses_id VARCHAR(255), stat VARCHAR(255), stat_rej_rsn VARCHAR(255), txt VARCHAR(255), CONSTRAINT pk_trading_session_status PRIMARY KEY (id));");
            createTables.add("CREATE SEQUENCE margin_component_seq;");
            createTables.add("CREATE SEQUENCE margin_shortfall_surplus_seq;");
            createTables.add("CREATE SEQUENCE total_margin_requirement_seq;");
            createTables.add("CREATE SEQUENCE trading_session_status_seq;");
            createTables.add("CREATE VIEW margin_component_latest AS SELECT t.* FROM (SELECT clearer, member, account, clss, ccy, MAX(received) AS received FROM MARGIN_COMPONENT GROUP BY clearer, member, account, clss, ccy) x JOIN MARGIN_COMPONENT t ON x.received =t.received AND t.clearer = x.clearer AND t.member = x.member AND t.account = x.account AND t.clss = x.clss AND x.ccy = t.ccy;");
            createTables.add("CREATE VIEW total_margin_requirement_latest AS SELECT t.* FROM (SELECT clearer, pool, member, account, ccy, MAX(received) AS received FROM TOTAL_MARGIN_REQUIREMENT GROUP BY clearer, pool, member, account, ccy) x JOIN TOTAL_MARGIN_REQUIREMENT t ON x.received =t.received AND t.clearer = x.clearer AND t.pool = x.pool AND t.member = x.member AND t.account = x.account AND x.ccy = t.ccy;");
            createTables.add("CREATE VIEW margin_shortfall_surplus_latest AS SELECT t.* FROM (SELECT clearer, pool, member, clearing_ccy, ccy, MAX(received) AS received FROM MARGIN_SHORTFALL_SURPLUS GROUP BY clearer, pool, member, clearing_ccy, ccy) x JOIN MARGIN_SHORTFALL_SURPLUS t ON x.received =t.received AND t.clearer = x.clearer AND t.pool = x.pool AND t.member = x.member AND t.clearing_ccy = x.clearing_ccy AND x.ccy = t.ccy;");

            jdbcConnection.batch(
                    createTables,
                    ar -> {
                        if (ar.failed()) {
                            LOG.error("Failed to create DB structure");
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

    private void startPersisting(Future<Void> fut)
    {
        EventBus eb = vertx.eventBus();

        eb.consumer("ers.TradingSessionStatus", message -> storeTradingSessionStatus(message));
    }

    private void storeTradingSessionStatus(Message msg)
    {
        LOG.info("Stroing TSS message with body: " + msg.body().toString());
        TradingSessionStatus tss = Json.decodeValue(msg.body().toString(), TradingSessionStatus.class);

        String sql = "INSERT INTO trading_session_status (req_id, ses_id, stat, stat_rej_rsn, txt) VALUES (?, ?, ?, ?, ?)";
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
                        LOG.info("Stored TradingSessionStatus into DB");
                    });
        });
    }

    @Override
    public void stop() throws Exception {
        jdbc.close();
    }
}
