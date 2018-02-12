set long 100000
set linesize 9999
set head off
set pagesize 0
set verify off
set feedback off

set timing on
set time on
set sqlprompt "  >  "
WHENEVER OSERROR EXIT SQL.SQLCODE rollback
WHENEVER SQLERROR EXIT SQL.SQLCODE rollback
set autocommit on
set define off

set echo on

alter session set "_add_col_optim_enabled"=false
/