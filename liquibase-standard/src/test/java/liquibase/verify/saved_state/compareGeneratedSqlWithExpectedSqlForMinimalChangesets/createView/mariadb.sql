-- Database: mariadb
-- Change Parameter: selectQuery=select id, name from person where id > 10
-- Change Parameter: viewName=v_person
CREATE VIEW v_person AS select id, name from person where id > 10;
