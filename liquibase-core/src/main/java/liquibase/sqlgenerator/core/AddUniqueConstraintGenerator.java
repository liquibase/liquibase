package liquibase.sqlgenerator.core;

import java.lang.reflect.Array;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddUniqueConstraintStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;
import liquibase.structure.core.UniqueConstraint;
import liquibase.util.StringUtils;

public class AddUniqueConstraintGenerator extends AbstractSqlGenerator<AddUniqueConstraintStatement> {

    // This change ensures we don't have auto-vivification of generic
    // arrays (which is generally a bad thing) due to varargs.
    @SuppressWarnings("unchecked")
    private static final Class<? extends Database>[] EMPTY_DATABASE_CLAZZ_ARRAY = (Class<? extends Database>[]) Array.newInstance(Class.class, 0);

    @Override
    public boolean supports(AddUniqueConstraintStatement statement, Database database) {
        return !(database instanceof SQLiteDatabase)
            && !(database instanceof MSSQLDatabase)
            && !(database instanceof SybaseDatabase)
            && !(database instanceof SybaseASADatabase)
            && !(database instanceof InformixDatabase);
    }

    @Override
    public ValidationErrors validate(AddUniqueConstraintStatement addUniqueConstraintStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("columnNames", addUniqueConstraintStatement.getColumnNames());
        validationErrors.checkRequiredField("tableName", addUniqueConstraintStatement.getTableName());

        if (!(database instanceof OracleDatabase)) {
            validationErrors.checkDisallowedField("forIndexName", addUniqueConstraintStatement.getForIndexName(), database, EMPTY_DATABASE_CLAZZ_ARRAY);
        }

        // if (!(database instanceof MSSQLDatabase) &&
        // addUniqueConstraintStatement.isClustered()) {
        // validationErrors.checkDisallowedField("clustered",
        // addUniqueConstraintStatement.isClustered(), database);
        // }
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(AddUniqueConstraintStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {

        String sql = null;
        if (statement.getConstraintName() == null) {
            sql = String.format("ALTER TABLE %s ADD UNIQUE (%s)"
                , database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
                , database.escapeColumnNameList(statement.getColumnNames())
                );
        } else {
            sql = String.format("ALTER TABLE %s ADD CONSTRAINT %s UNIQUE (%s)"
                , database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
                , database.escapeConstraintName(statement.getConstraintName())
                , database.escapeColumnNameList(statement.getColumnNames())
                );
        }

        final boolean isOracle = database instanceof OracleDatabase;
        final boolean isPostgres = database instanceof PostgresDatabase;

        if (isOracle || isPostgres) {
            if (statement.isDeferrable()) {
                sql += " DEFERRABLE";
            }

            if (statement.isInitiallyDeferred()) {
                sql += " INITIALLY DEFERRED";
            }
        }

        // Currently, only OracleDatabase supports the DISABLE command.
        if (isOracle && statement.isDisabled()) {
            sql += " DISABLE";
        }

        if (StringUtils.trimToNull(statement.getTablespace()) != null && database.supportsTablespaces()) {
            if (database instanceof MSSQLDatabase) {
                sql += " ON " + statement.getTablespace();
            } else if (database instanceof DB2Database
                || database instanceof SybaseASADatabase
                || database instanceof InformixDatabase) {
                ; // not supported
            } else {
                sql += " USING INDEX TABLESPACE " + statement.getTablespace();
            }
        }

        if (statement.getForIndexName() != null) {
            sql += " USING INDEX " + database.escapeObjectName(statement.getForIndexCatalogName(), statement.getForIndexSchemaName(), statement.getForIndexName(), Index.class);
        }

        return new Sql[] {
            new UnparsedSql(sql, getAffectedUniqueConstraint(statement))
        };

    }

    protected UniqueConstraint getAffectedUniqueConstraint(AddUniqueConstraintStatement statement) {
        UniqueConstraint uniqueConstraint = new UniqueConstraint()
            .setName(statement.getConstraintName())
            .setTable((Table) new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName()));
        int i = 0;
        for (Column column : Column.listFromNames(statement.getColumnNames())) {
            uniqueConstraint.addColumn(i++, column);
        }
        return uniqueConstraint;
    }
}
