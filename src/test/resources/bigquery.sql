--liquibase formatted sql

--changeset lolejniczak:create-a-table
create table authors
(
    dim_channel_key INT64,
    channel_name STRING,
    channel_type STRING,
    channel_sub_type STRING,
    storing_order INT64,
    channel_display_name STRING,
    channel_code  STRING,
    created_date timestamp,
    last_modified_date timestamp,
    oem_owner  STRING,
    publisher  STRING
)
;