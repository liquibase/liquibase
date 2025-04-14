-- liquibase formatted sql
-- changeset test:create-sequence-big
create sequence big
    as decimal (19)
    start with 100000000000
    increment by 1
    minvalue -9999999999999999999
    maxvalue 9999999999999999999;

-- changeset test:create-sequence-small
create sequence small
    start with 1
    increment by 1
    minvalue 0
    maxvalue 20;