package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DeleteStatement;
import liquibase.structure.core.Column;

public class DeleteGenerator extends AbstractSqlGenerator<DeleteStatement> {

    @Override
    public ValidationErrors validate(DeleteStatement deleteStatement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", deleteStatement.getTableName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DeleteStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        StringBuffer sql = new StringBuffer("DELETE FROM " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()));

        if (statement.getWhereClause() != null) {
            String fixedWhereClause = " WHERE " + statement.getWhereClause();
            for (String columnName : statement.getWhereColumnNames()) {
                if (columnName == null) {
                    continue;
                }
                fixedWhereClause = fixedWhereClause.replaceFirst(":name",
                        database.escapeObjectName(columnName, Column.class));
            }
            for (Object param : statement.getWhereParameters()) {
                fixedWhereClause = fixedWhereClause.replaceFirst("\\?|:value", DataTypeFactory.getInstance().fromObject(param, database).objectToSql(param, database).replaceAll("\\$", "\\$"));
            }
            sql.append(" ").append(fixedWhereClause);
        }

        return new Sql[]{new UnparsedSql(sql.toString())};
    }
}
