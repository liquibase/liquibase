package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropIndexStatement;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;

import java.util.List;

public class DropIndexGenerator extends AbstractSqlGenerator<DropIndexStatement> {

    @Override
    public ValidationErrors validate(DropIndexStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("indexName", statement.getIndexName());

        if ((database instanceof MySQLDatabase) || (database instanceof MSSQLDatabase) || database instanceof SybaseASADatabase) {
            validationErrors.checkRequiredField("tableName", statement.getTableName());
        }

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropIndexStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        List<String> associatedWith = StringUtils.splitAndTrim(statement.getAssociatedWith(), ",");
        if (associatedWith != null) {
            if (associatedWith.contains(Index.MARK_PRIMARY_KEY) || associatedWith.contains(Index.MARK_UNIQUE_CONSTRAINT)) {
                return new Sql[0];
            } else if (associatedWith.contains(Index.MARK_FOREIGN_KEY)) {
                if (!((database instanceof OracleDatabase) || (database instanceof MSSQLDatabase))) {
                    return new Sql[0];
                }
            }
        }

        String schemaName = statement.getTableSchemaName();

        if (database instanceof MySQLDatabase) {
            return new Sql[]{new UnparsedSql("DROP INDEX " + database.escapeIndexName(null, null, statement.getIndexName()) + " ON " + database.escapeTableName(statement.getTableCatalogName(), schemaName, statement.getTableName()), getAffectedIndex(statement))};
        } else if (database instanceof MSSQLDatabase) {
            return new Sql[]{new UnparsedSql("DROP INDEX " + database.escapeIndexName(null, null, statement.getIndexName()) + " ON " + database.escapeTableName(null, schemaName, statement.getTableName()), getAffectedIndex(statement))};
        } else if (database instanceof SybaseDatabase) {
            return new Sql[]{new UnparsedSql("DROP INDEX " + statement.getTableName() + "." + statement.getIndexName(), getAffectedIndex(statement))};
        } else if (database instanceof SybaseASADatabase) {
            return new Sql[]{new UnparsedSql("DROP INDEX " + database.escapeTableName(statement.getTableCatalogName(), schemaName, statement.getTableName()) + "." + database.escapeIndexName(statement.getTableCatalogName(), schemaName, statement.getIndexName()), getAffectedIndex(statement))};
        } else if (database instanceof PostgresDatabase) {
            return new Sql[]{new UnparsedSql("DROP INDEX " + database.escapeIndexName(statement.getTableCatalogName(), schemaName, statement.getIndexName()), getAffectedIndex(statement))};
        }

        return new Sql[]{new UnparsedSql("DROP INDEX " + database.escapeIndexName(statement.getTableCatalogName(), schemaName, statement.getIndexName()), getAffectedIndex(statement))};
    }

    protected Index getAffectedIndex(DropIndexStatement statement) {
        Table table = null;
        if (statement.getTableName() != null) {
            table = (Table) new Table().setName(statement.getTableName()).setSchema(statement.getTableCatalogName(), statement.getTableSchemaName());
        }
        return new Index().setName(statement.getIndexName()).setTable(table);
    }
}
