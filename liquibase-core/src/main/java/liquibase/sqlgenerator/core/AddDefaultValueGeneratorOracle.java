package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.executor.ExecutionOptions;
import liquibase.datatype.DataTypeFactory;
import liquibase.database.core.OracleDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddDefaultValueStatement;

public class AddDefaultValueGeneratorOracle extends AddDefaultValueGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddDefaultValueStatement statement, ExecutionOptions options) {
        return options.getRuntimeEnvironment().getTargetDatabase() instanceof OracleDatabase;
    }

    @Override
    public Sql[] generateSql(AddDefaultValueStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();
        Object defaultValue = statement.getDefaultValue();
        return new Sql[]{
                new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " MODIFY " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " DEFAULT " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database))
        };
    }
}