package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;
import liquibase.datatype.core.BooleanType;
import liquibase.datatype.core.CharType;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.core.AddDefaultValueStatement;

public class AddDefaultValueGenerator extends AbstractSqlGenerator<AddDefaultValueStatement> {

    @Override
    public ValidationErrors validate(AddDefaultValueStatement addDefaultValueStatement, ExecutionEnvironment env, StatementLogicChain chain) {
        Database database = env.getTargetDatabase();

        Object defaultValue = addDefaultValueStatement.getDefaultValue();

        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("defaultValue", defaultValue);
        validationErrors.checkRequiredField("columnName", addDefaultValueStatement.getColumnName());
        validationErrors.checkRequiredField("tableName", addDefaultValueStatement.getTableName());
        if (!database.supportsSequences() && defaultValue instanceof SequenceNextValueFunction) {
            validationErrors.addError("Database "+ database.getShortName()+" does not support sequences");
        }
        if (database instanceof HsqlDatabase) {
            if (defaultValue instanceof SequenceNextValueFunction) {
                validationErrors.addError("Database " + database.getShortName() + " does not support adding sequence-based default values");
            } else if (defaultValue instanceof DatabaseFunction) {
                validationErrors.addError("Database " + database.getShortName() + " does not support adding function-based default values");
            }
        }

        String columnDataType = addDefaultValueStatement.getColumnDataType();
        if (columnDataType != null) {
            LiquibaseDataType dataType = DataTypeFactory.getInstance().fromDescription(columnDataType, database);
            boolean typeMismatch = false;
            if (dataType instanceof BooleanType) {
                if (!(defaultValue instanceof Boolean)) {
                    typeMismatch = true;
                }
            } else if (dataType instanceof CharType) {
                if (!(defaultValue instanceof String)) {
                    typeMismatch = true;
                }
            }

            if (typeMismatch) {
                validationErrors.addError("Default value of "+defaultValue+" does not match defined type of "+columnDataType);
            }
        }

        return validationErrors;
    }

    @Override
    public Action[] generateActions(AddDefaultValueStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        Database database = env.getTargetDatabase();

        Object defaultValue = statement.getDefaultValue();
        return new Action[]{
                new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN  " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " SET DEFAULT " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database))
        };
    }
}
