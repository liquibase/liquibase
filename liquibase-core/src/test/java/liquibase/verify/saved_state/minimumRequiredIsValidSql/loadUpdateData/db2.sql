-- Database: db2
-- Change Parameter: file=com/example/users.csv
-- Database: db2
-- Change Parameter: primaryKey=A String
-- Database: db2
-- Change Parameter: tableName=person
BEGIN ATOMIC
	DECLARE v_reccount INTEGER;
	SET v_reccount = (SELECT COUNT(*) FROM person WHERE A String = NULL);
	IF v_reccount = 0 THEN
INSERT INTO person (username,  fullname) VALUES ('nvoxland', ' Nathan Voxland');
	ELSEIF v_reccount = 1 THEN
UPDATE person SET  fullname = ' Nathan Voxland', username = 'nvoxland' WHERE A String = NULL;
END IF;
END;
BEGIN ATOMIC
	DECLARE v_reccount INTEGER;
	SET v_reccount = (SELECT COUNT(*) FROM person WHERE A String = NULL);
	IF v_reccount = 0 THEN
INSERT INTO person (username,  fullname) VALUES ('bob', ' Bob Bobson');
	ELSEIF v_reccount = 1 THEN
UPDATE person SET  fullname = ' Bob Bobson', username = 'bob' WHERE A String = NULL;
END IF;
END;
