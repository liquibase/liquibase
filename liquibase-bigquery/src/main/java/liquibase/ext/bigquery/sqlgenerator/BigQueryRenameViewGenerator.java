package liquibase.ext.bigquery.sqlgenerator;

import liquibase.database.Database;
import liquibase.ext.bigquery.database.BigqueryDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.RenameViewGenerator;
import liquibase.statement.core.RenameViewStatement;
import liquibase.structure.DatabaseObject;

import static liquibase.ext.bigquery.database.BigqueryDatabase.BIGQUERY_PRIORITY_DATABASE;

public class BigQueryRenameViewGenerator extends RenameViewGenerator {

    public BigQueryRenameViewGenerator(){}

    @Override
    public int getPriority() {
        return BIGQUERY_PRIORITY_DATABASE;
    }


    public boolean supports(RenameViewStatement statement, Database database) {
        return database instanceof BigqueryDatabase;
    }

    public Sql[] generateSql(RenameViewStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[]{new UnparsedSql("ALTER VIEW " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldViewName()) + " RENAME TO " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getNewViewName()), new DatabaseObject[]{this.getAffectedOldView(statement), this.getAffectedNewView(statement)})};
    }
}
