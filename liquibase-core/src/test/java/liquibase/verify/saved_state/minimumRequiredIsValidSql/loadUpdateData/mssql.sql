-- Database: mssql
-- Change Parameter: file=com/example/users.csv
-- Change Parameter: primaryKey=A String
-- Change Parameter: tableName=person
DECLARE @reccount integer
SELECT @reccount = count(*) FROM [person] WHERE [A String] = NULL
IF @reccount = 0
BEGIN
INSERT INTO [person] ([username], [ fullname]) VALUES ('nvoxland', ' Nathan Voxland');
END
ELSE
BEGIN
UPDATE [person] SET [ fullname] = ' Nathan Voxland', [username] = 'nvoxland' WHERE [A String] = NULL;
END;
DECLARE @reccount integer
SELECT @reccount = count(*) FROM [person] WHERE [A String] = NULL
IF @reccount = 0
BEGIN
INSERT INTO [person] ([username], [ fullname]) VALUES ('bob', ' Bob Bobson');
END
ELSE
BEGIN
UPDATE [person] SET [ fullname] = ' Bob Bobson', [username] = 'bob' WHERE [A String] = NULL;
END;
