#!/usr/bin/env bash
set -e

mkdir -p /tmp/tablespace/liquibase2

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER lbuser WITH PASSWORD 'LiquibasePass1';
    CREATE TABLESPACE liquibase2 OWNER lbuser LOCATION '/tmp/tablespace/liquibase2';
    GRANT ALL PRIVILEGES ON DATABASE lbcat TO lbuser;
    GRANT ALL PRIVILEGES ON SCHEMA public TO lbuser;




DROP TABLE IF EXISTS authors;
CREATE TABLE authors (
  id SERIAL,
  first_name VARCHAR (50) NOT NULL,
  last_name VARCHAR (50) NOT NULL,
  email VARCHAR (100) NOT NULL,
  birthdate date NOT NULL,
  added timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

INSERT INTO authors VALUES ('1','Eileen','Lubowitz','ppaucek@example.org','1991-03-04','2004-05-30 02:08:25'),
('2','Tamia','Mayert','shansen@example.org','2016-03-27','2014-03-21 02:52:00'),
('3','Cyril','Funk','reynolds.godfrey@example.com','1988-04-21','2011-06-24 18:17:48'),
('4','Nicolas','Buckridge','xhoeger@example.net','2017-02-03','2019-04-22 02:04:41'),
('5','Jayden','Walter','lillian66@example.com','2010-02-27','1990-02-04 02:32:00');

ALTER TABLE authors OWNER TO lbuser;
GRANT ALL PRIVILEGES ON TABLE authors TO lbuser;


DROP TABLE IF EXISTS posts;
CREATE TABLE posts (
  id SERIAL,
  author_id INT NOT NULL,
  title VARCHAR (255) NOT NULL,
  description VARCHAR (500) NOT NULL,
  content text NOT NULL,
  inserted_date date
);

ALTER TABLE posts OWNER TO lbuser;
GRANT ALL PRIVILEGES ON TABLE posts TO lbuser;

INSERT INTO posts VALUES ('1','1','temporibus','voluptatum','Fugit non et doloribus repudiandae.','2015-11-18'),
('2','2','ea','aut','Tempora molestias maiores provident molestiae sint possimus quasi.','1975-06-08'),
('3','3','illum','rerum','Delectus recusandae sit officiis dolor.','1975-02-25'),
('4','4','itaque','deleniti','Magni nam optio id recusandae.','2010-07-28'),
('5','5','ad','similique','Rerum tempore quis ut nesciunt qui excepturi est.','2006-10-09');




EOSQL