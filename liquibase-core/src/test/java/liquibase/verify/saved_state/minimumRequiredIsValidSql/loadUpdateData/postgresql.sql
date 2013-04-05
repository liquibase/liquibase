-- Database: postgresql
-- Change Parameter: file=com/example/users.csv
-- Change Parameter: primaryKey=A String
-- Change Parameter: tableName=person
DO
$$
BEGIN
UPDATE person SET  fullname = ' Nathan Voxland', username = 'nvoxland' WHERE "A String" = NULL;
IF not found THEN
INSERT INTO person (username,  fullname) VALUES ('nvoxland', ' Nathan Voxland');
END IF;
END;
$$
LANGUAGE plpgsql;;
DO
$$
BEGIN
UPDATE person SET  fullname = ' Bob Bobson', username = 'bob' WHERE "A String" = NULL;
IF not found THEN
INSERT INTO person (username,  fullname) VALUES ('bob', ' Bob Bobson');
END IF;
END;
$$
LANGUAGE plpgsql;;
