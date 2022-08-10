--liquibase formatted sql

--changeset ewojtach:create-a-table
CREATE TABLE stg_customer (
                              CUSTOMER_ID INTEGER,
                              FIRST_NAME STRING,
                              LAST_NAME STRING
);
;