-- Database: db2
-- Change Parameter: columns=[column:[
--     name="id"
--     type="int"
-- ], ]
-- Change Parameter: file=com/example/users.csv
-- Change Parameter: primaryKey=pk_id
-- Change Parameter: tableName=person
BEGIN ATOMIC
	DECLARE v_reccount INTEGER;
	SET v_reccount = (SELECT COUNT(*) FROM person WHERE pk_id = NULL);
	IF v_reccount = 0 THEN
INSERT INTO person (username, fullname) VALUES ('nvoxland', ' Nathan Voxland');
	ELSEIF v_reccount = 1 THEN
UPDATE person SET fullname = ' Nathan Voxland', username = 'nvoxland' WHERE pk_id = NULL;
END IF;
END;
BEGIN ATOMIC
	DECLARE v_reccount INTEGER;
	SET v_reccount = (SELECT COUNT(*) FROM person WHERE pk_id = NULL);
	IF v_reccount = 0 THEN
INSERT INTO person (username, fullname) VALUES ('bob', ' Bob Bobson');
	ELSEIF v_reccount = 1 THEN
UPDATE person SET fullname = ' Bob Bobson', username = 'bob' WHERE pk_id = NULL;
END IF;
END;
