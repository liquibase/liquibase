-- liquibase formatted sql

-- changeset "Some User":1::createTable splitStatements:true labels:setup endDelimiter:; dbms:mssql
CREATE TABLE PRIMARY_TABLE (NAME VARCHAR(20), LASTNAME VARCHAR(20));

-- Comment on TABLE
EXEC sp_addextendedproperty
     @name = N'MS_Description', @value = 'COMMENT 1. THIS IS A COMMENT ON PRIMARY_TABLE TABLE',
     @level0type = N'Schema', @level0name = 'dbo',
     @level1type = N'Table', @level1name = 'PRIMARY_TABLE';

-- Comment on TABLE column 'NAME'
EXEC sp_addextendedproperty
     @name = N'MS_Description', @value = 'COMMENT 2. THIS IS A COMMENT ON PRIMARY_TABLE.NAME COLUMN',
     @level0type = N'Schema', @level0name = 'dbo',
     @level1type = N'Table', @level1name = 'PRIMARY_TABLE',
     @level2type = N'Column', @level2name = 'NAME';

-- Comment on TABLE column 'LASTNAME'
EXEC sp_addextendedproperty
     @name = N'MS_Description', @value = 'COMMENT 3. THIS IS A COMMENT ON PRIMARY_TABLE.LASTNAME COLUMN',
     @level0type = N'Schema', @level0name = 'dbo',
     @level1type = N'Table', @level1name = 'PRIMARY_TABLE',
     @level2type = N'Column', @level2name = 'LASTNAME';


-- changeset "Some User":1::createView splitStatements:true labels:setup endDelimiter:; dbms:mssql
CREATE VIEW SOME_VIEW AS SELECT A.NAME, A.LASTNAME FROM PRIMARY_TABLE A GROUP BY A.NAME, A.LASTNAME;

-- Comment on VIEW
EXEC sp_addextendedproperty
     @name = N'MS_Description', @value = 'COMMENT 4. THIS IS A COMMENT ON SOME_VIEW VIEW. THIS VIEW COMMENT SHOULD BE CAPTURED.',
     @level0type = N'Schema', @level0name = 'dbo',
     @level1type = N'View', @level1name = 'SOME_VIEW';

-- Comment on VIEW column 'NAME'
EXEC sp_addextendedproperty
     @name = N'MS_Description', @value = 'COMMENT 5. THIS IS A COMMENT ON SOME_VIEW.NAME VIEW COLUMN. THIS VIEW COLUMN COMMENT SHOULD BE CAPTURED.',
     @level0type = N'Schema', @level0name = 'dbo',
     @level1type = N'View', @level1name = 'SOME_VIEW',
     @level2type = N'Column', @level2name = 'NAME';

-- Comment on VIEW column 'LASTNAME'
EXEC sp_addextendedproperty
     @name = N'MS_Description', @value = 'COMMENT 6. THIS IS A COMMENT ON SOME_VIEW.LASTNAME VIEW COLUMN. THIS VIEW COLUMN COMMENT SHOULD BE CAPTURED.',
     @level0type = N'Schema', @level0name = 'dbo',
     @level1type = N'View', @level1name = 'SOME_VIEW',
     @level2type = N'Column', @level2name = 'LASTNAME';
