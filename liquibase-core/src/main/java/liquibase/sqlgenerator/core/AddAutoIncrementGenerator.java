package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.AddAutoIncrementStatement;

public class AddAutoIncrementGenerator extends AbstractSqlGenerator<AddAutoIncrementStatement> {

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean supports(AddAutoIncrementStatement statement, ExecutionEnvironment env) {
        Database database = env.getTargetDatabase();

        return (database.supportsAutoIncrement()
                && !(database instanceof DerbyDatabase)
                && !(database instanceof MSSQLDatabase)
                && !(database instanceof HsqlDatabase)
                && !(database instanceof H2Database));
    }

    @Override
    public ValidationErrors validate(
    		AddAutoIncrementStatement statement,
    		ExecutionEnvironment env,
    		StatementLogicChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("columnName", statement.getColumnName());
        validationErrors.checkRequiredField("tableName", statement.getTableName());
        validationErrors.checkRequiredField("columnDataType", statement.getColumnDataType());


        return validationErrors;
    }

    @Override
    public Action[] generateActions(AddAutoIncrementStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        Database database = env.getTargetDatabase();

        String sql = "ALTER TABLE "
            + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
            + " MODIFY "
            + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName())
            + " "
            + DataTypeFactory.getInstance().fromDescription(statement.getColumnDataType() + "{autoIncrement:true}", database).toDatabaseDataType(database)
            + " " 
            + database.getAutoIncrementClause(statement.getStartWith(), statement.getIncrementBy());

        return new Action[]{
            new UnparsedSql(sql)
        };
    }
}
