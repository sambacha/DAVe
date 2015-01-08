# --- !Ups

CREATE VIEW margin_component_latest AS SELECT t.* FROM (SELECT clearer, member, account, clss, MAX(received) AS received
    FROM MARGIN_COMPONENT
    GROUP BY clearer, member, account, clss, ccy) x
    JOIN MARGIN_COMPONENT t ON x.received =t.received
    AND t.clearer = x.clearer AND t.member = x.member AND t.account = x.account AND t.clss = x.clss;

CREATE VIEW total_margin_requirement_latest AS SELECT t.* FROM (SELECT clearer, pool, member, account, MAX(received) AS received
    FROM TOTAL_MARGIN_REQUIREMENT
    GROUP BY clearer, pool, member, account, ccy) x
    JOIN TOTAL_MARGIN_REQUIREMENT t ON x.received =t.received
    AND t.clearer = x.clearer AND t.pool = x.pool AND t.member = x.member AND t.account = x.account;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

DROP VIEW IF EXISTS margin_component_latest;
DROP VIEW IF EXISTS total_margin_requirement_latest;

SET REFERENTIAL_INTEGRITY TRUE;
