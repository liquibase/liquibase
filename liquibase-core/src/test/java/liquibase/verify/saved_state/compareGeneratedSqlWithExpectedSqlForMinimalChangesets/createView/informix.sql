-- Database: informix
-- Change Parameter: selectQuery=SELECT id, name FROM person WHERE id > 10
-- Change Parameter: viewName=v_person
CREATE VIEW  v_person AS SELECT * FROM (SELECT id, name FROM person WHERE id > 10) AS v;
