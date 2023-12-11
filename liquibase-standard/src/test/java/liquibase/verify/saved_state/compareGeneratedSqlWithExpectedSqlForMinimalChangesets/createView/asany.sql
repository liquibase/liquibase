-- Database: asany
-- Change Parameter: selectQuery=SELECT id, name FROM person WHERE id > 10
-- Change Parameter: viewName=v_person
SET TEMPORARY OPTION force_view_creation='ON';
CREATE VIEW v_person AS SELECT id, name FROM person WHERE id > 10;
