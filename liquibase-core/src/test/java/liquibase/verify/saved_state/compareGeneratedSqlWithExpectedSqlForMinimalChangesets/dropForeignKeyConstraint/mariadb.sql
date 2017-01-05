-- Database: mariadb
-- Change Parameter: baseTableName=person
-- Change Parameter: constraintName=fk_address_person
ALTER TABLE person DROP FOREIGN KEY fk_address_person;
