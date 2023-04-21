-- Database: postgresql
-- Change Parameter: columns=[column:[
--     name="id"
--     type="int"
-- ], ]
-- Change Parameter: file=com/example/users.csv
-- Change Parameter: primaryKey=pk_id
-- Change Parameter: tableName=person
DO
$$
BEGIN
UPDATE person SET fullname = ' Nathan Voxland', username = 'nvoxland' WHERE pk_id = NULL;
IF not found THEN
INSERT INTO person (username, fullname) VALUES ('nvoxland', ' Nathan Voxland');
END IF;
END;
$$
LANGUAGE plpgsql;;
DO
$$
BEGIN
UPDATE person SET fullname = ' Bob Bobson', username = 'bob' WHERE pk_id = NULL;
IF not found THEN
INSERT INTO person (username, fullname) VALUES ('bob', ' Bob Bobson');
END IF;
END;
$$
LANGUAGE plpgsql;;
