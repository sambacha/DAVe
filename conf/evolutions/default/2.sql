# --- !Ups

CREATE VIEW margin_component_latest AS SELECT t.* FROM (SELECT clearer, member, account, clss, MAX(received) AS received
    FROM MARGIN_COMPONENT
    GROUP BY clearer, member, account, clss, ccy) x
    JOIN MARGIN_COMPONENT t ON x.received =t.received
    AND t.clearer = x.clearer AND t.member = x.member AND t.account = x.account AND t.clss = x.clss

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop view if exists margin_component_latest;

SET REFERENTIAL_INTEGRITY TRUE;
