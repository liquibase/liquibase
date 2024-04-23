package liquibase.ext.bigquery.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.ext.bigquery.database.BigqueryDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.RenameTableGenerator;
import liquibase.statement.core.DeleteStatement;
import liquibase.statement.core.RenameTableStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;

import static liquibase.ext.bigquery.database.BigqueryDatabase.BIGQUERY_PRIORITY_DATABASE;

public class BigQueryRenameTableGenerator extends RenameTableGenerator {
    @Override
    public int getPriority() {
        return BIGQUERY_PRIORITY_DATABASE;
    }


    @Override
    public Sql[] generateSql(RenameTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String sql;
        sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getOldTableName()) + " RENAME TO " + database.escapeObjectName(statement.getNewTableName(), Table.class);

        return new Sql[]{new UnparsedSql(sql, new DatabaseObject[]{this.getAffectedOldTable(statement), this.getAffectedNewTable(statement)})};
    }

}
