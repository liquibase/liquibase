-- Database: mssql
-- Change Parameter: columns=[column:[
--     name="id"
--     type="int"
-- ], ]
-- Change Parameter: file=com/example/users.csv
-- Change Parameter: primaryKey=pk_id
-- Change Parameter: tableName=person
DECLARE @reccount integer
SELECT @reccount = count(*) FROM person WHERE pk_id = NULL
IF @reccount = 0
BEGIN
INSERT INTO person (username,  fullname) VALUES ('nvoxland', ' Nathan Voxland');
END
ELSE
BEGIN
UPDATE person SET  fullname = ' Nathan Voxland', username = 'nvoxland' WHERE pk_id = NULL;
END;
DECLARE @reccount integer
SELECT @reccount = count(*) FROM person WHERE pk_id = NULL
IF @reccount = 0
BEGIN
INSERT INTO person (username,  fullname) VALUES ('bob', ' Bob Bobson');
END
ELSE
BEGIN
UPDATE person SET  fullname = ' Bob Bobson', username = 'bob' WHERE pk_id = NULL;
END;
