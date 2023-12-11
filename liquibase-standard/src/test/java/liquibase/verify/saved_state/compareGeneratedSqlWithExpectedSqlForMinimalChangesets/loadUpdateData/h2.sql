-- Database: h2
-- Change Parameter: columns=[column:[
--     name="id"
--     type="int"
-- ], ]
-- Change Parameter: file=com/example/users.csv
-- Change Parameter: primaryKey=pk_id
-- Change Parameter: tableName=person
MERGE INTO person (username, fullname) KEY(pk_id) VALUES ('nvoxland', ' Nathan Voxland');;
MERGE INTO person (username, fullname) KEY(pk_id) VALUES ('bob', ' Bob Bobson');;
