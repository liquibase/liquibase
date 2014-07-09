package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
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
    public boolean supports(AddColumnStatement statement, ExecutionEnvironment env) {
        Database database = env.getTargetDatabase();

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
    public ValidationErrors validate(AddColumnStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        Database database = env.getTargetDatabase();

        ValidationErrors validationErrors = super.validate(statement, env, chain);
        if (database instanceof DerbyDatabase && statement.isAutoIncrement()) {
            validationErrors.addError("Cannot add an identity column to derby");
        }
        return validationErrors;
    }

    @Override
    public Action[] generateActions(AddColumnStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        Database database = env.getTargetDatabase();

        String alterTable = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ADD " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + DataTypeFactory.getInstance().fromDescription(statement.getColumnType() + (statement.isAutoIncrement() ? "{autoIncrement:true}" : ""), database).toDatabaseDataType(database);

        alterTable += getDefaultClause(statement, env);

        if (primaryKeyBeforeNotNull(env)) {
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

        if (!primaryKeyBeforeNotNull(env)) {
            if (statement.isPrimaryKey()) {
                alterTable += " PRIMARY KEY";
            }
        }

        List<Action> returnSql = new ArrayList<Action>();
        returnSql.add(new UnparsedSql(alterTable));

        addUniqueConstrantStatements(statement, env, returnSql);
        addForeignKeyStatements(statement, env, returnSql);

        return returnSql.toArray(new Action[returnSql.size()]);
    }


    private String getDefaultClause(AddColumnStatement statement, ExecutionEnvironment env) {
        Database database = env.getTargetDatabase();
        String clause = "";
        Object defaultValue = statement.getDefaultValue();
        if (defaultValue != null) {
            clause += " DEFAULT " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database);
        }
        return clause;
    }

    private boolean primaryKeyBeforeNotNull(ExecutionEnvironment env) {
        Database database = env.getTargetDatabase();

        return !(database instanceof HsqlDatabase || database instanceof H2Database);
    }


}
