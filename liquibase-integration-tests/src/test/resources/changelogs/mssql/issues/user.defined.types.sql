--changeset user:add-type
CREATE TYPE dbo.Flag FROM bit NOT NULL

--changeset user:use-type
CREATE TABLE dbo.test(
    flag Flag not null
)