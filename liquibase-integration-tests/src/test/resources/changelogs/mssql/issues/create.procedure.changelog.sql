-- liquibase formatted sql
--changeset mallod:endDelimiterTest runOnChange:true stripComments:false splitStatements:true endDelimiter:;;;;;
create or alter procedure dbo.TestProcedure
    as
select top 5 *
from
    dbo.CustomerMaster
where
        TerritoryID = 2
  and IsActive = 1
;;;;;