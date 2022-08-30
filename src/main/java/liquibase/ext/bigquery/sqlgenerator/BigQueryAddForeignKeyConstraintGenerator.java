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
        if (!supports(statement, database)) {
            return sqlGeneratorChain.generateSql(statement, database);
        }
        String sql;
        sql = "";

        return new Sql[]{
                new UnparsedSql(sql, getAffectedForeignKey(statement))
        };
    }
}
