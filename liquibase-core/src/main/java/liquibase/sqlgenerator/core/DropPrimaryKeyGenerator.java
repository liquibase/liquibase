package liquibase.sqlgenerator.core;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropPrimaryKeyStatement;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;

public class DropPrimaryKeyGenerator extends AbstractSqlGenerator<DropPrimaryKeyStatement> {

    @Override
    public boolean supports(DropPrimaryKeyStatement statement, Database database) {
        return (!(database instanceof SQLiteDatabase));
    }

    @Override
    public ValidationErrors validate(DropPrimaryKeyStatement dropPrimaryKeyStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", dropPrimaryKeyStatement.getTableName());

        if (database instanceof FirebirdDatabase || database instanceof InformixDatabase) {
            validationErrors.checkRequiredField("constraintName", dropPrimaryKeyStatement.getConstraintName());
        }
		
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropPrimaryKeyStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String sql;

        if (database instanceof MSSQLDatabase) {
			if (statement.getConstraintName() == null) {
				String schemaName = statement.getSchemaName();
				if (schemaName == null) {
					schemaName = database.getDefaultSchemaName();
				}
				schemaName = StringUtils.trimToNull(schemaName);

				StringBuilder query = new StringBuilder();
				query.append("DECLARE @pkname nvarchar(255)");
				query.append("\n");
				query.append("DECLARE @sql nvarchar(2048)");
				query.append("\n");
				query.append("select @pkname=i.name from sysindexes i");
				query.append(" join sysobjects o ON i.id = o.id");
				query.append(" join sysobjects pk ON i.name = pk.name AND pk.parent_obj = i.id AND pk.xtype = 'PK'");
				query.append(" join sysindexkeys ik on i.id = ik.id AND i.indid = ik.indid");
				query.append(" join syscolumns c ON ik.id = c.id AND ik.colid = c.colid");
				query.append(" INNER JOIN sysusers su ON o.uid = su.uid");
				query.append(" where o.name = '").append(statement.getTableName()).append("'");
				query.append(" and su.name='").append(schemaName).append("'");
				query.append("\n");
				query.append("set @sql='alter table ").append(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())).append(" drop constraint ' + @pkname");
				query.append("\n");
				query.append("exec(@sql)");
				query.append("\n");
				sql = query.toString();
			} else {
				sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " DROP CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName());
			}
        } else if (database instanceof PostgresDatabase) {
			if (statement.getConstraintName() == null) {
                String schemaName = new CatalogAndSchema(null, statement.getSchemaName()).customize(database).getSchemaName();

                StringBuilder query = new StringBuilder();
				query.append("create or replace function __liquibase_drop_pk(schemaName text, tableName text) returns void as $$");
				query.append(" declare");
				query.append(" pkname text;");
				query.append(" sql text;");
				query.append(" begin");
				query.append(" pkname = c.conname");
				query.append(" from pg_class r, pg_constraint c, pg_catalog.pg_namespace n");
				query.append(" where r.oid = c.conrelid");
				query.append(" and contype = 'p'");
                query.append(" and n.oid = r.relnamespace");
                query.append(" and nspname ilike schemaName");
				query.append(" and relname ilike tableName;");
				query.append(" sql = 'alter table ' || schemaName || '.' || tableName || ' drop constraint ' || pkname;");
				query.append(" execute sql;");
				query.append(" end;");
				query.append(" $$ language plpgsql;");
                query.append(" select __liquibase_drop_pk('").append(schemaName).append("', '").append(statement.getTableName()).append("');");
				query.append(" drop function __liquibase_drop_pk(schemaName text, tableName text);");
				sql = query.toString();			
			} else {
				sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " DROP CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName());
			}
        } else if (database instanceof FirebirdDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " DROP CONSTRAINT "+database.escapeConstraintName(statement.getConstraintName());
        } else if (database instanceof OracleDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " DROP PRIMARY KEY DROP INDEX";
        } else if (database instanceof InformixDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " DROP CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName());
        } else {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " DROP PRIMARY KEY";
        }

        return new Sql[] {
                new UnparsedSql(sql, getAffectedPrimaryKey(statement))
        };
    }

    protected PrimaryKey getAffectedPrimaryKey(DropPrimaryKeyStatement statement) {
        return new PrimaryKey().setName(statement.getConstraintName()).setTable((Table) new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName()));
    }
}
