package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statement.core.DeleteDataStatement;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.structure.core.Column;

public class DeleteGenerator extends AbstractSqlGenerator<DeleteDataStatement> {

    @Override
    public ValidationErrors validate(DeleteDataStatement deleteDataStatement, ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", deleteDataStatement.getTableName());
        return validationErrors;
    }

    @Override
    public Action[] generateActions(DeleteDataStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        Database database = env.getTargetDatabase();

        StringBuffer sql = new StringBuffer("DELETE FROM " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()));

        if (statement.getWhere() != null) {
            String fixedWhereClause = " WHERE " + statement.getWhere();
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

        return new Action[]{new UnparsedSql(sql.toString())};
    }
}
