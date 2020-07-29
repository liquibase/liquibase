create database lbcat2;

GRANT ALL PRIVILEGES ON lbcat.* TO 'lbuser'@'%';
GRANT ALL PRIVILEGES ON lbcat2.* TO 'lbuser'@'%';

ALTER USER 'lbuser'@'%' IDENTIFIED WITH mysql_native_password BY 'LiquibasePass1';
/*try update mysql-connector-java to 8.0.21 to avoid this hack*/

