package liquibase.sqlgenerator;

import liquibase.database.BigqueryDatabase;
import liquibase.database.Database;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.core.DropForeignKeyConstraintGenerator;
import liquibase.statement.core.DropForeignKeyConstraintStatement;

public class BigQueryDropForeignKeyConstraintGenerator extends DropForeignKeyConstraintGenerator {

    @Override
    public int getPriority() {
        return SqlGenerator.PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(DropForeignKeyConstraintStatement statement, Database database) {
        return database instanceof BigqueryDatabase;
    }

    @Override
    public Sql[] generateSql(DropForeignKeyConstraintStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String sql = "SELECT 1";
        return new Sql[]{
                new UnparsedSql(sql, getAffectedForeignKey(statement))
        };
    }
}
