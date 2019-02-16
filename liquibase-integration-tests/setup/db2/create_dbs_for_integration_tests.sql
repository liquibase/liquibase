
/*
 * The following script can be executed using the "DB2 CLP" program after software installation. Be advised that
 * IBM DB2 has a strange database user concept: none. Every database login corresponds to an operating system login,
 * so in order to create the lbuser, you have to create an actual user "lbuser" on your Unix/Windows installation,
 * and then use operating system tools to set its password.
 */

CREATE DATABASE liquibas AUTOMATIC STORAGE YES ON 'D:\' DBPATH ON 'D:\'

CONNECT TO liquibas

GRANT CONNECT ON DATABASE TO USER LBUSER;
GRANT DBADM ON DATABASE TO USER LBUSER;

CREATE SCHEMA lbuser;
CREATE SCHEMA liquibase;

CREATE REGULAR TABLESPACE liquibase2 MANAGED BY AUTOMATIC STORAGE
GRANT USE OF TABLESPACE liquibase2 TO USER LBUSER
