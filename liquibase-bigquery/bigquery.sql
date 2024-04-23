--liquibase formatted sql

--changeset myuser:create-a-table

create table liquibase_test_harness.dim_channels
(
dim_channel_key smallint,
channel_name  STRING(40),
channel_type STRING(40) NOT NULL,
channel_sub_type  STRING(40) NOT NULL,
storing_order integer NOT NULL,
channel_display_name  STRING(40) NOT NULL,
channel_code  STRING(40) NOT NULL,
created_date timestamp, ##Note that a TIMESTAMP itself does not have a time zone
last_modified_date timestamp,
oem_owner  STRING(40) NOT NULL,
publisher  STRING(40) NOT NULL
)
;