package liquibase.sqlgenerator.core;

import liquibase.change.Change;
import liquibase.change.core.TagDatabaseChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.column.LiquibaseColumn;
import liquibase.changelog.values.ChangeLogColumnValueProvider;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.InsertStatement;
import liquibase.statement.core.MarkChangeSetRanStatement;
import liquibase.statement.core.UpdateStatement;

import java.util.Map;

public class MarkChangeSetRanGenerator extends AbstractSqlGenerator<MarkChangeSetRanStatement> {

    @Override
    public ValidationErrors validate(MarkChangeSetRanStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("changeSet", statement.getChangeSet());

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(MarkChangeSetRanStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ChangeSet changeSet = statement.getChangeSet();

        Map<String, ChangeLogColumnValueProvider> columnValuesProviders = statement.getColumnValuesProviders();

        SqlStatement runStatement;
        try {
            if (statement.getExecType().equals(ChangeSet.ExecType.FAILED) || statement.getExecType().equals(ChangeSet.ExecType.SKIPPED)) {
                return new Sql[0]; //don't mark
            }

            String tag = null;
            for (Change change : changeSet.getChanges()) {
                if (change instanceof TagDatabaseChange) {
                    TagDatabaseChange tagChange = (TagDatabaseChange) change;
                    tag = tagChange.getTag();
                }
            }

            if (statement.getExecType().ranBefore) {
                UpdateStatement updateStatement = new UpdateStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName());

                for(String columnName : statement.getColumnsForUpdate()) {
                    updateStatement.addNewColumnValue(columnName, columnValuesProviders.get(columnName).getValue(statement, database));
                }

                updateStatement
                        .setWhereClause(database.escapeObjectName("ID", LiquibaseColumn.class) + " = ? " +
                        "AND " + database.escapeObjectName("AUTHOR", LiquibaseColumn.class) + " = ? " +
                        "AND " + database.escapeObjectName("FILENAME", LiquibaseColumn.class) + " = ?")
                        .addWhereParameters(changeSet.getId(), changeSet.getAuthor(), changeSet.getFilePath());

                if (tag != null) {
                    updateStatement.addNewColumnValue("TAG", tag);
                }

                runStatement = updateStatement;
            } else {
                InsertStatement insertStatement = new InsertStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName());

                for(String columnName : statement.getColumnsForInsert()) {
                    insertStatement.addColumnValue(columnName, columnValuesProviders.get(columnName).getValue(statement, database));
                }

                if (tag != null) {
                    insertStatement.addColumnValue("TAG", tag);
                }

                runStatement = insertStatement;
            }
        } catch (LiquibaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }

        return SqlGeneratorFactory.getInstance().generateSql(runStatement, database);
    }

}
