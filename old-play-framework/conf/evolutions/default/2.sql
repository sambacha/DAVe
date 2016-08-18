# --- !Ups

CREATE VIEW margin_component_latest AS SELECT t.* FROM (SELECT clearer, member, account, clss, ccy, MAX(received) AS received
    FROM MARGIN_COMPONENT
    GROUP BY clearer, member, account, clss, ccy) x
    JOIN MARGIN_COMPONENT t ON x.received =t.received
    AND t.clearer = x.clearer AND t.member = x.member AND t.account = x.account AND t.clss = x.clss AND x.ccy = t.ccy;

CREATE VIEW total_margin_requirement_latest AS SELECT t.* FROM (SELECT clearer, pool, member, account, ccy, MAX(received) AS received
    FROM TOTAL_MARGIN_REQUIREMENT
    GROUP BY clearer, pool, member, account, ccy) x
    JOIN TOTAL_MARGIN_REQUIREMENT t ON x.received =t.received
    AND t.clearer = x.clearer AND t.pool = x.pool AND t.member = x.member AND t.account = x.account AND x.ccy = t.ccy;

CREATE VIEW margin_shortfall_surplus_latest AS SELECT t.* FROM (SELECT clearer, pool, member, clearing_ccy, ccy, MAX(received) AS received
    FROM MARGIN_SHORTFALL_SURPLUS
    GROUP BY clearer, pool, member, clearing_ccy, ccy) x
    JOIN MARGIN_SHORTFALL_SURPLUS t ON x.received =t.received
    AND t.clearer = x.clearer AND t.pool = x.pool AND t.member = x.member AND t.clearing_ccy = x.clearing_ccy AND x.ccy = t.ccy;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

DROP VIEW IF EXISTS margin_component_latest;
DROP VIEW IF EXISTS total_margin_requirement_latest;
DROP VIEW IF EXISTS margin_shortfall_surplus_latest;

SET REFERENTIAL_INTEGRITY TRUE;
