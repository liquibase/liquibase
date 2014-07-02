package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statement.core.CopyDataStatement;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.change.ColumnConfig;
import liquibase.database.core.SQLiteDatabase;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;

public class CopyDataGenerator extends AbstractSqlGenerator<CopyDataStatement> {

    @Override
    public boolean supports(CopyDataStatement statement, ExecutionEnvironment env) {
        return (env.getTargetDatabase() instanceof SQLiteDatabase);
    }

    @Override
    public ValidationErrors validate(CopyDataStatement copyDataStatement, ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("targetTableName", copyDataStatement.getTargetTableName());
        validationErrors.checkRequiredField("sourceTableName", copyDataStatement.getSourceTableName());
        validationErrors.checkRequiredField("sourceColumns", copyDataStatement.getSourceColumns());
        return validationErrors;
    }

    @Override
    public Action[] generateActions(CopyDataStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        StringBuffer sql = new StringBuffer();
        if (env.getTargetDatabase() instanceof SQLiteDatabase) {
            sql.append("INSERT INTO `").append(statement.getTargetTableName()).append("` (");

            for (int i = 0; i < statement.getSourceColumns().size(); i++) {
                ColumnConfig column = statement.getSourceColumns().get(i);
                if (i > 0) {
                    sql.append(",");
                }
                sql.append("`").append(column.getName()).append("`");
            }

            sql.append(") SELECT ");
            for (int i = 0; i < statement.getSourceColumns().size(); i++) {
                ColumnConfig column = statement.getSourceColumns().get(i);
                if (i > 0) {
                    sql.append(",");
                }
                sql.append("`").append(column.getName()).append("`");
            }
            sql.append(" FROM `").append(statement.getSourceTableName()).append("`");
        }

        return new Action[]{
                new UnparsedSql(sql.toString())
        };
    }
}
