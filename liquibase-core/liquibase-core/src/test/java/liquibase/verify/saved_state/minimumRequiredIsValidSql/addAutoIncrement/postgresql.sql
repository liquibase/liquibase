-- Database: postgresql
-- Change Parameter: columnName=id
-- Change Parameter: tableName=person
CREATE SEQUENCE person_id_seq;
ALTER TABLE person ALTER COLUMN  id SET NOT NULL;
ALTER TABLE person ALTER COLUMN  id SET DEFAULT nextval('person_id_seq');
ALTER SEQUENCE person_id_seq OWNED BY person.id;
