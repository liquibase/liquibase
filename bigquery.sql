--liquibase formatted sql

--changeset myuser:create-a-table

create table liquibase.dim_channels
(
dim_channel_key smallint NOT NULL,
channel_name character varying(40) NOT NULL,
channel_type character varying(20) NOT NULL,
channel_sub_type character varying(20) NOT NULL,
storing_order integer NOT NULL,
channel_display_name character varying(40) NOT NULL,
channel_code character varying(20) NOT NULL,
created_date timestamp without time zone NOT NULL,
last_modified_date timestamp without time zone NOT NULL,
oem_owner character varying(50) NOT NULL,
publisher character varying(100) NOT NULL
)
;