-- Database: hsqldb
-- Change Parameter: columns=[column:[
--     name="id"
--     type="int"
-- ], ]
-- Change Parameter: file=com/example/users.csv
-- Change Parameter: primaryKey=pk_id
-- Change Parameter: tableName=person
MERGE INTO person USING (VALUES (1)) ON pk_id = NULL WHEN NOT MATCHED THEN INSERT (username, fullname) VALUES ('nvoxland',' Nathan Voxland') WHEN MATCHED THEN UPDATE SET username = 'nvoxland', fullname = ' Nathan Voxland';
MERGE INTO person USING (VALUES (1)) ON pk_id = NULL WHEN NOT MATCHED THEN INSERT (username, fullname) VALUES ('bob',' Bob Bobson') WHEN MATCHED THEN UPDATE SET username = 'bob', fullname = ' Bob Bobson';
