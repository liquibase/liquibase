-- Database: informix
-- Change Parameter: selectQuery=select id, name from person where id > 10
-- Database: informix
-- Change Parameter: viewName=A String
CREATE VIEW  A String AS SELECT * FROM (select id, name from person where id > 10) AS v;
