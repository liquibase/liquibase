
/* WARNING: You need to adjust the path in CREATE DATABASE for this script to work. This script needs to be
   execute inside an "isql" prompt.
 */

CREATE DATABASE 'd:\temp\liquibase.fdb' page_size 8192 user 'sysdba' password 'liquibase';

CONNECT localhost:d:\temp\liquibase.fdb USER sysdba PASSWORD 'liquibase';

CREATE USER lbuser PASSWORD 'lbuser';

grant create table to lbuser;
grant create view to lbuser;
grant create procedure to lbuser;
grant create function to lbuser;
grant create sequence to lbuser;
grant create package to lbuser;