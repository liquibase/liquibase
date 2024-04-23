package liquibase.ext.bigquery.sqlgenerator;

import liquibase.database.Database;
import liquibase.ext.bigquery.database.BigqueryDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
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
