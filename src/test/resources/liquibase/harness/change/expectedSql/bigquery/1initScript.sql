DROP TABLE IF EXISTS authors
CREATE TABLE authors (
id INTEGER NOT NULL,
first_name STRING NOT NULL,
last_name STRING NOT NULL,
email STRING NOT NULL,
birthdate DATE NOT NULL,
added TIMESTAMP NOT NULL
)
INSERT INTO authors VALUES (1,'Eileen','Lubowitz','ppaucek@example.org','1991-03-04','2004-05-30 02:08:25')
INSERT INTO authors VALUES (2,'Tamia','Mayert','shansen@example.org','2016-03-27','2014-03-21 02:52:00')
DROP TABLE IF EXISTS posts
CREATE TABLE posts (
id INTEGER NOT NULL,
author_id INTEGER NOT NULL,
title STRING NOT NULL,
description STRING NOT NULL,
content STRING NOT NULL,
inserted_date DATE
)
INSERT INTO posts VALUES (1,1,'temporibus','voluptatum','Fugit non et doloribus repudiandae.','2015-11-18')
INSERT INTO posts VALUES (2,2,'ea','aut','Tempora molestias maiores provident molestiae sint possimus quasi.','1975-06-08')