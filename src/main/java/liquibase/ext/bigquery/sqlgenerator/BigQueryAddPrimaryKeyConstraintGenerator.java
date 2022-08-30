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
        if (!supports(statement, database)) {
            return sqlGeneratorChain.generateSql(statement, database);
        }
        String sql;
        sql = "";

        return new Sql[]{
                new UnparsedSql(sql, getAffectedPrimaryKey(statement))
        };
    }
}
