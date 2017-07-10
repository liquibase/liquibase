package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AddAutoIncrementStatement;

public class AddAutoIncrementGeneratorHsqlH2 extends AddAutoIncrementGenerator {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddAutoIncrementStatement statement, Database database) {
        return (database instanceof HsqlDatabase) || (database instanceof H2Database);
    }

    @Override
    public Sql[] generateSql(
            AddAutoIncrementStatement statement,
            Database database,
            SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[]{
            new UnparsedSql(
                "ALTER TABLE "
                    + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName())
                    + " ALTER COLUMN "
                    + database.escapeColumnName(
                        statement.getCatalogName(),
                        statement.getSchemaName(),
                        statement.getTableName(),
                        statement.getColumnName())
                    + " "
                    + DataTypeFactory.getInstance().fromDescription(statement.getColumnDataType(), database)
                    + " "
                    + database.getAutoIncrementClause(
                        statement.getStartWith(), statement.getIncrementBy()),
                getAffectedColumn(statement))
        };
    }
}
