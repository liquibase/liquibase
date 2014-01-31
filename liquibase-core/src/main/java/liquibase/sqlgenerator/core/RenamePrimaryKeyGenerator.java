package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.RenamePrimaryKeyStatement;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;

public class RenamePrimaryKeyGenerator extends AbstractSqlGenerator<RenamePrimaryKeyStatement> {

    @Override
    public boolean supports(RenamePrimaryKeyStatement statement, Database database) {
        // TODO support for other DBs
        return (database instanceof OracleDatabase) || (database instanceof PostgresDatabase);
    }

    @Override
    public ValidationErrors validate(RenamePrimaryKeyStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", statement.getTableName());
        validationErrors.checkRequiredField("newConstraintName", statement.getNewConstraintName());

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(RenamePrimaryKeyStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        
        final String tableNameEscaped = database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName());
        final String newConstraintNameEscaped = database.escapeConstraintName(statement.getNewConstraintName());
        String sql;
        
        if (database instanceof PostgresDatabase) {
          if (statement.getOldConstraintName() == null) {
            StringBuilder query = new StringBuilder();
            query.append("create or replace function __liquibase_rename_pk(tableName text, newConstraintName text) returns void as $$");
            query.append(" declare");
            query.append(" pkname text;");
            query.append(" sql text;");
            query.append(" begin");
            query.append(" pkname = c.conname");
            query.append(" from pg_class r, pg_constraint c");
            query.append(" where r.oid = c.conrelid");
            query.append(" and contype = 'p'");
            query.append(" and relname ilike tableName;");
            query.append(" sql = 'alter table ' || tableName || ' rename constraint ' || pkname || ' to ' || newConstraintName;");
            query.append(" execute sql;");
            query.append(" end;");
            query.append(" $$ language plpgsql;");
            query.append(" select __liquibase_rename_pk('").append(statement.getTableName()).append("','").append(newConstraintNameEscaped).append("');");
            query.append(" drop function __liquibase_rename_pk(tableName text, newConstraintName text);");
            sql = query.toString();     
          } else {
            sql = "ALTER TABLE " + tableNameEscaped 
                + " RENAME CONSTRAINT " + database.escapeConstraintName(statement.getOldConstraintName()) 
                + " TO " + newConstraintNameEscaped;
          }
        } else if (database instanceof OracleDatabase) {
          if (statement.getOldConstraintName() == null) {
            StringBuilder query = new StringBuilder();
            query.append(" DECLARE");
            query.append("   pk_name VARCHAR(30);");
            query.append("   sql_stmt VARCHAR(255);");
            query.append(" BEGIN");
            query.append("   SELECT constraint_name");
            query.append("     INTO pk_name");
            query.append("     FROM user_constraints");
            query.append("     WHERE UPPER(table_name) like UPPER('").append(statement.getTableName()).append("') and constraint_type = 'P';");
            query.append("   sql_stmt := 'ALTER TABLE ").append(tableNameEscaped).append(" RENAME CONSTRAINT ' || pk_name || ' TO ").append(newConstraintNameEscaped).append("';");
            query.append("   EXECUTE IMMEDIATE sql_stmt;");
            query.append(" END;");
            sql = query.toString();  
          } else {
            sql = "ALTER TABLE " + tableNameEscaped 
                + " RENAME CONSTRAINT " + database.escapeConstraintName(statement.getOldConstraintName()) 
                + " TO " + newConstraintNameEscaped;
          }
        } else {
          sql = "ALTER TABLE " + tableNameEscaped 
              + " RENAME CONSTRAINT " + database.escapeConstraintName(statement.getOldConstraintName()) 
              + " TO " + newConstraintNameEscaped;
        }

        return new Sql[] {
                new UnparsedSql(sql, getAffectedPrimaryKey(statement))
        };
    }

    protected PrimaryKey getAffectedPrimaryKey(RenamePrimaryKeyStatement statement) {
        return new PrimaryKey().setName(statement.getOldConstraintName()).setTable((Table) new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName()));
    }
}
