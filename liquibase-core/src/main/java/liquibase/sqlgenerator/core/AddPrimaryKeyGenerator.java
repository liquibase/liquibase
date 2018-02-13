package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.database.core.MockDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddPrimaryKeyStatement;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;
import liquibase.util.StringUtil;

public class AddPrimaryKeyGenerator extends AbstractSqlGenerator<AddPrimaryKeyStatement> {

    @Override
    public boolean supports(AddPrimaryKeyStatement statement, Database database) {
        return (!(database instanceof SQLiteDatabase));
    }

    @Override
    public ValidationErrors validate(AddPrimaryKeyStatement addPrimaryKeyStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("columnNames", addPrimaryKeyStatement.getColumnNames());
        validationErrors.checkRequiredField("tableName", addPrimaryKeyStatement.getTableName());

        if (!((database instanceof MSSQLDatabase) || (database instanceof MockDatabase))) {
            if ((addPrimaryKeyStatement.isClustered() != null) && !addPrimaryKeyStatement.isClustered()) {
                validationErrors.checkDisallowedField("clustered", addPrimaryKeyStatement.isClustered(), database);
            }
        }

        if (!((database instanceof OracleDatabase) || (database instanceof AbstractDb2Database))) {
            validationErrors.checkDisallowedField("forIndexName", addPrimaryKeyStatement.getForIndexName(), database);
        }

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(AddPrimaryKeyStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String sql;
        if ((statement.getConstraintName() == null) || (database instanceof MySQLDatabase) || (database instanceof
            SybaseASADatabase)) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ADD PRIMARY KEY (" + database.escapeColumnNameList(statement.getColumnNames()) + ")";
        } else {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ADD CONSTRAINT " + database.escapeConstraintName(statement.getConstraintName())+" PRIMARY KEY";
            if ((database instanceof MSSQLDatabase) && (statement.isClustered() != null)) {
                if (statement.isClustered()) {
                    sql += " CLUSTERED";
                } else {
                    sql += " NONCLUSTERED";
                }
            }
            sql += " (" + database.escapeColumnNameList(statement.getColumnNames()) + ")";
        }

        if ((StringUtil.trimToNull(statement.getTablespace()) != null) && database.supportsTablespaces()) {
            if (database instanceof MSSQLDatabase) {
                sql += " ON "+statement.getTablespace();
            } else if ((database instanceof AbstractDb2Database) || (database instanceof SybaseASADatabase)) {
                ; //not supported
            } else {
                sql += " USING INDEX TABLESPACE "+statement.getTablespace();
            }
        }

        if ((database instanceof OracleDatabase) && (statement.getForIndexName() != null)) {
            sql += " USING INDEX "+database.escapeObjectName(statement.getForIndexCatalogName(), statement.getForIndexSchemaName(), statement.getForIndexName(), Index.class);
        }

        if ((database instanceof PostgresDatabase) && (statement.isClustered() != null) && statement.isClustered() &&
            (statement.getConstraintName() != null)) {
            return new Sql[] {
                    new UnparsedSql(sql, getAffectedPrimaryKey(statement)),
                    new UnparsedSql("CLUSTER "+database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())+" USING "+database.escapeObjectName(statement.getConstraintName(), PrimaryKey.class))
            };
        } else {
            return new Sql[] {
                    new UnparsedSql(sql, getAffectedPrimaryKey(statement))
            };
        }
    }

    protected PrimaryKey getAffectedPrimaryKey(AddPrimaryKeyStatement statement) {
        return new PrimaryKey().setTable((Table) new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName()));
    }
}
