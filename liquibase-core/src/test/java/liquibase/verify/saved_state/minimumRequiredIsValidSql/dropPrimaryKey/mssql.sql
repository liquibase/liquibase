-- Database: mssql
-- Change Parameter: tableName=person
DECLARE @pkname nvarchar(255)
DECLARE @sql nvarchar(2048)
select @pkname=i.name from sysindexes i join sysobjects o ON i.id = o.id join sysobjects pk ON i.name = pk.name AND pk.parent_obj = i.id AND pk.xtype = 'PK' join sysindexkeys ik on i.id = ik.id AND i.indid = ik.indid join syscolumns c ON ik.id = c.id AND ik.colid = c.colid where o.name = 'person'
set @sql='alter table [person] drop constraint ' + @pkname
exec(@sql);
