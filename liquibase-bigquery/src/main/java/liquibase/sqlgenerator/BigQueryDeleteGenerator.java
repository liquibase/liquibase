package liquibase.sqlgenerator;

import liquibase.database.BigQueryDatabase;
import liquibase.database.Database;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.core.DeleteGenerator;
import liquibase.statement.core.DeleteStatement;

import static liquibase.util.SqlUtil.replacePredicatePlaceholders;

public class BigQueryDeleteGenerator extends DeleteGenerator {

    @Override
    public int getPriority() {
        return BigQueryDatabase.BIGQUERY_PRIORITY_DATABASE;
    }

    @Override
    public Sql[] generateSql(DeleteStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {

        StringBuilder sql = new StringBuilder("DELETE FROM ")
                .append(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()));

        if (statement.getWhere() != null) {
            sql.append(" WHERE ").append(replacePredicatePlaceholders(database, statement.getWhere(), statement.getWhereColumnNames(), statement.getWhereParameters()));
        }
        // bigquery Each time you construct a DELETE statement, you must use the WHERE keyword, followed by a condition.
        else {
            sql.append(" WHERE 1 = 1");
        }

        return new Sql[]{new UnparsedSql(sql.toString(), getAffectedTable(statement))};
    }

    @Override
    public boolean supports(DeleteStatement statement, Database database) {
        return database instanceof BigQueryDatabase;
    }
}
