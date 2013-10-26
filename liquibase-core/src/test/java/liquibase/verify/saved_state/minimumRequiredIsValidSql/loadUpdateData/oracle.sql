-- Database: oracle
-- Change Parameter: columns=[column:[
--     name="id"
--     type="int"
-- ], ]
-- Change Parameter: file=com/example/users.csv
-- Change Parameter: primaryKey=pk_id
-- Change Parameter: tableName=person
DECLARE
	v_reccount NUMBER := 0;
BEGIN
	SELECT COUNT(*) INTO v_reccount FROM person WHERE pk_id = NULL;
	IF v_reccount = 0 THEN
INSERT INTO person (username, fullname) VALUES ('nvoxland', ' Nathan Voxland');
	ELSIF v_reccount = 1 THEN
UPDATE person SET fullname = ' Nathan Voxland', username = 'nvoxland' WHERE pk_id = NULL;
END IF;
END;;
DECLARE
	v_reccount NUMBER := 0;
BEGIN
	SELECT COUNT(*) INTO v_reccount FROM person WHERE pk_id = NULL;
	IF v_reccount = 0 THEN
INSERT INTO person (username, fullname) VALUES ('bob', ' Bob Bobson');
	ELSIF v_reccount = 1 THEN
UPDATE person SET fullname = ' Bob Bobson', username = 'bob' WHERE pk_id = NULL;
END IF;
END;;
