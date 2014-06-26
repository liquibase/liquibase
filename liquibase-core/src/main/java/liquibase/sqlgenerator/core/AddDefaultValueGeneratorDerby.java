package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.datatype.DataTypeFactory;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.AddDefaultValueStatement;

public class AddDefaultValueGeneratorDerby extends AddDefaultValueGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddDefaultValueStatement statement, ExecutionEnvironment env) {
        return env.getTargetDatabase() instanceof DerbyDatabase;
    }

    @Override
    public Action[] generateActions(AddDefaultValueStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        Database database = env.getTargetDatabase();
        Object defaultValue = statement.getDefaultValue();
        return new Action[]{
                new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN  " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " WITH DEFAULT " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database))
        };
    }
}