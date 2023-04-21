-- Database: mysql
-- Change Parameter: columns=[column:[
--     name="id"
--     type="int"
-- ], ]
-- Change Parameter: file=com/example/users.csv
-- Change Parameter: primaryKey=pk_id
-- Change Parameter: tableName=person
INSERT INTO person (username, fullname) VALUES ('nvoxland', ' Nathan Voxland')
ON DUPLICATE KEY UPDATE username = 'nvoxland', fullname = ' Nathan Voxland';
INSERT INTO person (username, fullname) VALUES ('bob', ' Bob Bobson')
ON DUPLICATE KEY UPDATE username = 'bob', fullname = ' Bob Bobson';
