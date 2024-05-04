CREATE OR ALTER PROCEDURE dbo.Test as
BEGIN TRAN
CREATE TABLE test.dbo.person
(
    id       int primary key not null,
    name     varchar(50)     not null,
    address1 varchar(50),
    address2 varchar(50),
    city     varchar(30)
);
INSERT INTO test.dbo.person(id, name)
VALUES(1, 'Name1');
COMMIT

    INSERT INTO test.dbo.person(id, name)
    VALUES(2, 'Name2');
GO
