package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.UnparsedSql;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.AutoIncrementConstraint;
import liquibase.statement.core.AddColumnStatement;

import java.util.ArrayList;
import java.util.List;

public class AddColumnGeneratorDefaultClauseBeforeNotNull extends AddColumnGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddColumnStatement statement, ExecutionOptions options) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        return database instanceof OracleDatabase
                || database instanceof HsqlDatabase
                || database instanceof H2Database
                || database instanceof DerbyDatabase
                || database instanceof DB2Database
                || database instanceof FirebirdDatabase
                || database instanceof SybaseDatabase
                || database instanceof SybaseASADatabase
                || database instanceof InformixDatabase;
    }

    @Override
    public ValidationErrors validate(AddColumnStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        ValidationErrors validationErrors = super.validate(statement, options, sqlGeneratorChain);
        if (database instanceof DerbyDatabase && statement.isAutoIncrement()) {
            validationErrors.addError("Cannot add an identity column to derby");
        }
        return validationErrors;
    }

    @Override
    public Action[] generateActions(AddColumnStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        String alterTable = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ADD " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + DataTypeFactory.getInstance().fromDescription(statement.getColumnType() + (statement.isAutoIncrement() ? "{autoIncrement:true}" : ""), database).toDatabaseDataType(database);

        alterTable += getDefaultClause(statement, options);

        if (primaryKeyBeforeNotNull(options)) {
            if (statement.isPrimaryKey()) {
                alterTable += " PRIMARY KEY";
            }
        }

        if (statement.isAutoIncrement()) {
            AutoIncrementConstraint autoIncrementConstraint = statement.getAutoIncrementConstraint();
            alterTable += " " + database.getAutoIncrementClause(autoIncrementConstraint.getStartWith(), autoIncrementConstraint.getIncrementBy());
        }

        if (!statement.isNullable()) {
            alterTable += " NOT NULL";
        } else if (database instanceof SybaseDatabase || database instanceof SybaseASADatabase) {
            alterTable += " NULL";
        }

        if (!primaryKeyBeforeNotNull(options)) {
            if (statement.isPrimaryKey()) {
                alterTable += " PRIMARY KEY";
            }
        }

        List<Action> returnSql = new ArrayList<Action>();
        returnSql.add(new UnparsedSql(alterTable));

        addUniqueConstrantStatements(statement, options, returnSql);
        addForeignKeyStatements(statement, options, returnSql);

        return returnSql.toArray(new Action[returnSql.size()]);
    }


    private String getDefaultClause(AddColumnStatement statement, ExecutionOptions options) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();
        String clause = "";
        Object defaultValue = statement.getDefaultValue();
        if (defaultValue != null) {
            clause += " DEFAULT " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database);
        }
        return clause;
    }

    private boolean primaryKeyBeforeNotNull(ExecutionOptions options) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();

        return !(database instanceof HsqlDatabase || database instanceof H2Database);
    }


}
