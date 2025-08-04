package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.core.AddDefaultValueStatement;

public class AddDefaultValueGeneratorHsqldb extends AddDefaultValueGenerator {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddDefaultValueStatement statement, Database database) {
        return database instanceof HsqlDatabase;
    }

    @Override
    public Sql[] generateSql(AddDefaultValueStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {

        if (!(statement.getDefaultValue() instanceof SequenceNextValueFunction)) {
            return super.generateSql(statement, database, sqlGeneratorChain);
        }
        Object defaultValue = statement.getDefaultValue();
        String sql = String.format("ALTER TABLE %s ALTER COLUMN %s %s",
                database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()),
                database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()),
                DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database)
        );

        UnparsedSql unparsedSql = new UnparsedSql(sql, getAffectedColumn(statement));
        return new Sql[]{unparsedSql};
    }
}
