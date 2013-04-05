-- Database: mysql
-- Change Parameter: file=com/example/users.csv
-- Database: mysql
-- Change Parameter: primaryKey=A String
-- Database: mysql
-- Change Parameter: tableName=person
INSERT INTO person (username,  fullname) VALUES ('nvoxland', ' Nathan Voxland')
ON DUPLICATE KEY UPDATE username = 'username', fullname = ' fullname';
INSERT INTO person (username,  fullname) VALUES ('bob', ' Bob Bobson')
ON DUPLICATE KEY UPDATE username = 'username', fullname = ' fullname';
