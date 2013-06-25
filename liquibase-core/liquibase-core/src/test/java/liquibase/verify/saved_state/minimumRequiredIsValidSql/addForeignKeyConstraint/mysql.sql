-- Database: mysql
-- Change Parameter: baseColumnNames=person_id
-- Change Parameter: baseTableName=address
-- Change Parameter: constraintName=fk_address_person
-- Change Parameter: referencedColumnNames=id
-- Change Parameter: referencedTableName=person
ALTER TABLE address ADD CONSTRAINT fk_address_person FOREIGN KEY (person_id) REFERENCES person (id);
