CREATE PROCEDURE dbo.A
    AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @handle UNIQUEIDENTIFIER;

BEGIN DIALOG @handle
        FROM SERVICE [//aa/BB/Service]
        TO SERVICE '//aa/BB/Service'
        ON CONTRACT [//aa/BB/Contract]
        WITH ENCRYPTION = OFF;

    SEND ON CONVERSATION @handle MESSAGE TYPE [//aa/BB/Type];

END;
GO
CREATE PROCEDURE dbo.B
    AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @queue TABLE (message_type_name VARCHAR(256));

    WAITFOR (
            RECEIVE message_type_name FROM dbo.que_bindata
            INTO @queue
            );

DELETE
FROM dbo.data_tmp
WHERE start_date< DATEADD(DAY, - 1, GETDATE());
END;
GO
CREATE PROCEDURE dbo.C
    AS
BEGIN
    SET NOCOUNT ON;

    BEGIN DISTRIBUTED TRANSACTION;
    -- Delete candidate from local instance.
    DELETE dbo.HumanResources.JobCandidate
    COMMIT TRANSACTION;
END;
GO