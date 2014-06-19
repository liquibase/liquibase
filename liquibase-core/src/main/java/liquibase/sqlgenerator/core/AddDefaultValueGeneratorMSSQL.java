package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.executor.ExecutionOptions;
import liquibase.datatype.DataTypeFactory;
import liquibase.database.core.MSSQLDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddDefaultValueStatement;

public class AddDefaultValueGeneratorMSSQL extends AddDefaultValueGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddDefaultValueStatement statement, ExecutionOptions options) {
        return options.getRuntimeEnvironment().getTargetDatabase() instanceof MSSQLDatabase;
    }

    @Override
    public Sql[] generateSql(AddDefaultValueStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        Object defaultValue = statement.getDefaultValue();
        Database database = options.getRuntimeEnvironment().getTargetDatabase();
        return new Sql[] {
                new UnparsedSql("ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ADD CONSTRAINT " + ((MSSQLDatabase) database).generateDefaultConstraintName(statement.getTableName(), statement.getColumnName()) + " DEFAULT " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database) + " FOR " + statement.getColumnName())
        };
    }
}