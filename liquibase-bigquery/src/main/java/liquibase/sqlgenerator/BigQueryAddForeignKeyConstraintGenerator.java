package liquibase.sqlgenerator;

import liquibase.database.BigqueryDatabase;
import liquibase.database.Database;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.core.AddForeignKeyConstraintGenerator;
import liquibase.statement.core.AddForeignKeyConstraintStatement;

public class BigQueryAddForeignKeyConstraintGenerator extends AddForeignKeyConstraintGenerator {

    @Override
    public int getPriority() {
        return SqlGenerator.PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddForeignKeyConstraintStatement statement, Database database) {
        return database instanceof BigqueryDatabase;
    }

    @Override
    public Sql[] generateSql(AddForeignKeyConstraintStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String sql = "SELECT 1";
        return new Sql[]{
                new UnparsedSql(sql, getAffectedForeignKey(statement))
        };
    }
}
