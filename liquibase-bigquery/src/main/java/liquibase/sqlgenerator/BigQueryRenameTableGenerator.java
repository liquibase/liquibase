package liquibase.sqlgenerator;

import liquibase.database.BigqueryDatabase;
import liquibase.database.Database;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.core.RenameTableGenerator;
import liquibase.statement.core.RenameTableStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;

public class BigQueryRenameTableGenerator extends RenameTableGenerator {
    @Override
    public int getPriority() {
        return BigqueryDatabase.BIGQUERY_PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(RenameTableStatement statement, Database database) {
        return database instanceof BigqueryDatabase;
    }

    @Override
    public Sql[] generateSql(RenameTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String sql;
        sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldTableName()) + " RENAME TO " + database.escapeObjectName(statement.getNewTableName(), Table.class);

        return new Sql[]{new UnparsedSql(sql, new DatabaseObject[]{this.getAffectedOldTable(statement), this.getAffectedNewTable(statement)})};
    }

}
