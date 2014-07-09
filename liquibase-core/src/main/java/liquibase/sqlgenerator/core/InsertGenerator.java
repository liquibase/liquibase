package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statement.core.InsertDataStatement;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.DatabaseFunction;

import java.util.Date;

public class InsertGenerator extends AbstractSqlGenerator<InsertDataStatement> {

    @Override
    public ValidationErrors validate(InsertDataStatement insertDataStatement, ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", insertDataStatement.getTableName());
        validationErrors.checkRequiredField("columns", insertDataStatement.getColumnNames());

//        if (insertStatement.getSchemaName() != null && !database.supportsSchemas()) {
//           validationErrors.addError("Database does not support schemas");
//       }

        return validationErrors;
    }

    @Override
    public Action[] generateActions(InsertDataStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        Database database = env.getTargetDatabase();

        StringBuffer sql = new StringBuffer("INSERT INTO " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " (");
        for (String column : statement.getColumnNames()) {
            sql.append(database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), column)).append(", ");
        }
        sql.deleteCharAt(sql.lastIndexOf(" "));
        int lastComma = sql.lastIndexOf(",");
        if (lastComma >= 0) {
            sql.deleteCharAt(lastComma);
        }

        sql.append(") VALUES (");

        for (String column : statement.getColumnNames()) {
            Object newValue = statement.getColumnValue(column);
            if (newValue == null || newValue.toString().equalsIgnoreCase("NULL")) {
                sql.append("NULL");
            } else if (newValue instanceof String && !looksLikeFunctionCall(((String) newValue), database)) {
                sql.append(DataTypeFactory.getInstance().fromObject(newValue, database).objectToSql(newValue, database));
            } else if (newValue instanceof Date) {
                sql.append(database.getDateLiteral(((Date) newValue)));
            } else if (newValue instanceof Boolean) {
                if (((Boolean) newValue)) {
                    sql.append(DataTypeFactory.getInstance().getTrueBooleanValue(database));
                } else {
                    sql.append(DataTypeFactory.getInstance().getFalseBooleanValue(database));
                }
            } else if (newValue instanceof DatabaseFunction) {
                sql.append(database.generateDatabaseFunctionValue((DatabaseFunction) newValue));
            }
            else {
                sql.append(newValue);
            }
            sql.append(", ");
        }

        sql.deleteCharAt(sql.lastIndexOf(" "));
        lastComma = sql.lastIndexOf(",");
        if (lastComma >= 0) {
            sql.deleteCharAt(lastComma);
        }

        sql.append(")");

        return new Action[] {
                new UnparsedSql(sql.toString())
        };
    }
}
