/* Create LBCAT database */
DROP DATABASE IF EXISTS lbcat;
CREATE DATABASE lbcat
  DEFAULT CHARACTER SET utf8;

/* Create LIQUIBASE database */
DROP DATABASE IF EXISTS liquibase;
CREATE DATABASE liquibase
  DEFAULT CHARACTER SET utf8;

/* Create LBCAT2 database */
DROP DATABASE IF EXISTS lbcat2;
CREATE DATABASE lbcat2
  DEFAULT CHARACTER SET utf8;

DROP USER IF EXISTS 'lbuser'@'localhost';

CREATE USER 'lbuser'@'localhost' IDENTIFIED BY 'lbuser';
GRANT ALL PRIVILEGES ON lbcat.* TO 'lbuser'@'localhost';
GRANT ALL PRIVILEGES ON lbcat2.* TO 'lbuser'@'localhost';
GRANT ALL PRIVILEGES ON liquibase.* TO 'lbuser'@'localhost';
FLUSH PRIVILEGES;

