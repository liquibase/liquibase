package liquibase.sqlgenerator.core;

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

    @Override
    public boolean supports(AddUniqueConstraintStatement statement, Database database) {
        return !(database instanceof SQLiteDatabase)
                && !(database instanceof SybaseDatabase)
                && !(database instanceof SybaseASADatabase)
                && !(database instanceof InformixDatabase)
                ;
    }

    @Override
    public ValidationErrors validate(AddUniqueConstraintStatement addUniqueConstraintStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("columnNames", addUniqueConstraintStatement.getColumnNames());
        validationErrors.checkRequiredField("tableName", addUniqueConstraintStatement.getTableName());

        if (!(database instanceof OracleDatabase)) {
            validationErrors.checkDisallowedField("forIndexName", addUniqueConstraintStatement.getForIndexName(), database);
        }

        if (!(database instanceof MSSQLDatabase) && addUniqueConstraintStatement.isClustered()) {
            validationErrors.checkDisallowedField("clustered", addUniqueConstraintStatement.isClustered(), database);
        }
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(AddUniqueConstraintStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {

        String sql;
        if (statement.getConstraintName() == null) {
            sql = String.format("ALTER TABLE %s ADD UNIQUE" + (statement.isClustered() ? " CLUSTERED " : " ") + "(%s)"
                    , database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
                    , database.escapeColumnNameList(statement.getColumnNames())
            );
        } else {
            sql = String.format("ALTER TABLE %s ADD CONSTRAINT %s UNIQUE" + (statement.isClustered() ? " CLUSTERED " : " ") + "(%s)"
                    , database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
                    , database.escapeConstraintName(statement.getConstraintName())
                    , database.escapeColumnNameList(statement.getColumnNames())
            );
        }
        if ((database instanceof OracleDatabase) || (database instanceof PostgresDatabase)) {
            if (statement.isDeferrable()) {
                sql += " DEFERRABLE";
            }
            if (statement.isInitiallyDeferred()) {
                sql += " INITIALLY DEFERRED";
            }
        }

        if ((database instanceof OracleDatabase) &&  statement.isDisabled()) {
            sql += " DISABLE";
        }

        boolean isInUsingIndexClause = false;

        if (statement.getForIndexName() != null) {
            sql += " USING INDEX ";
            sql += database.escapeObjectName(statement.getForIndexCatalogName(), statement.getForIndexSchemaName(),
                statement.getForIndexName(), Index.class);
            isInUsingIndexClause = true;
        }

        if ((StringUtils.trimToNull(statement.getTablespace()) != null) && database.supportsTablespaces()) {
            if (database instanceof MSSQLDatabase) {
                sql += " ON " + statement.getTablespace();
            } else if ((database instanceof AbstractDb2Database) || (database instanceof SybaseASADatabase) || (database
                instanceof InformixDatabase)) {
                ; //not supported
            } else if (database instanceof OracleDatabase) {
                /*
                 * In Oracle, you can use only exactly one of these clauses:
                 * 1. USING INDEX (identifier)
                 * 2. USING INDEX (index attributes) <-- Note that NO identifier is allowed in this form!
                 * 3. USING INDEX (CREATE INDEX (identifier) TABLESPACE (tablespace) (further attributes...) )
                 * However, if an index name _is_ present, we can assume that CreateIndexGenerator picked it up before
                 * this generator is called, so we really only need the second form at this point.
                */
                if (statement.getForIndexName() == null)
                    sql += " USING INDEX TABLESPACE " + statement.getTablespace();
            } else {
                if (!isInUsingIndexClause)
                    sql += " USING INDEX";
                sql += " TABLESPACE " + statement.getTablespace();
            }
        }

        if (database instanceof OracleDatabase) {
            sql += !statement.shouldValidate() ? " ENABLE NOVALIDATE " : "";
        }

        return new Sql[]{
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
