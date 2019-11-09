-- Database: informix
-- Change Parameter: selectQuery=select id, name from person where id > 10
-- Change Parameter: viewName=v_person
CREATE VIEW  v_person AS SELECT * FROM (select id, name from person where id > 10) AS v;
