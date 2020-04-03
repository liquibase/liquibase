-- Database: firebird
-- Change Parameter: columnName=id
-- Change Parameter: tableName=person
UPDATE RDB$RELATION_FIELDS SET RDB$NULL_FLAG = NULL WHERE RDB$RELATION_NAME = 'person' AND RDB$FIELD_NAME = 'id';
