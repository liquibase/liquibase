package liquibase.ext.bigquery.sqlgenerator;

import liquibase.database.Database;
import liquibase.ext.bigquery.database.BigqueryDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AddPrimaryKeyGenerator;
import liquibase.statement.core.AddPrimaryKeyStatement;

public class BigQueryAddPrimaryKeyConstraintGenerator extends AddPrimaryKeyGenerator {

    @Override
    public int getPriority() {
        return SqlGenerator.PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AddPrimaryKeyStatement statement, Database database) {
        return database instanceof BigqueryDatabase;
    }

    @Override
    public Sql[] generateSql(AddPrimaryKeyStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String sql = "SELECT 1";

        return new Sql[]{
                new UnparsedSql(sql, getAffectedPrimaryKey(statement))
        };
    }
}
