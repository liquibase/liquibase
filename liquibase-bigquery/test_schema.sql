DROP SCHEMA IF EXISTS liquibase_test_harness CASCADE;
CREATE SCHEMA liquibase_test_harness;

# create authors
CREATE TABLE `upheld-modem-345014.liquibase_test_harness.authors`
(
  id INT64 NOT NULL,
  first_name STRING(255) NOT NULL,
  last_name STRING(255),
  email STRING(100),
  birthdate STRING(255),
  added TIMESTAMP
);

#insert data

INSERT INTO `upheld-modem-345014.liquibase_test_harness.authors` VALUES (1,'Eileen','Lubowitz','ppaucek@example.org','1991-03-04','2004-05-30 02:08:25'),
                                                                        (2,'Tamia','Mayert','shansen@example.org','2016-03-27','2014-03-21 02:52:00'),
                                                                        (3,'Cyril','Funk','reynolds.godfrey@example.com','1988-04-21','2011-06-24 18:17:48'),
                                                                        (4,'Nicolas','Buckridge','xhoeger@example.net','2017-02-03','2019-04-22 02:04:41'),
                                                                        (5,'Jayden','Walter','lillian66@example.com','2010-02-27','1990-02-04 02:32:00');

#create posts

CREATE TABLE `upheld-modem-345014.liquibase_test_harness.posts`
(
  id INT64 NOT NULL,
  author_id INT64 NOT NULL,
  title STRING(255),
  description STRING(255),
  content STRING(255),
  inserted_date TIMESTAMP
);

#insert data posts

INSERT INTO `upheld-modem-345014.liquibase_test_harness.posts` VALUES (1, 1,'temporibus','voluptatum','Fugit non et doloribus repudiandae.','2015-11-18'),
                                                                      (2, 2,'ea','aut','Tempora molestias maiores provident molestiae sint possimus quasi.','1975-06-08'),
                                                                      (3, 3,'illum','rerum','Delectus recusandae sit officiis dolor.','1975-02-25'),
                                                                      (4, 4,'itaque','deleniti','Magni nam optio id recusandae.','2010-07-28'),
                                                                      (5, 5,'ad','similique','Rerum tempore quis ut nesciunt qui excepturi est.','2006-10-09');