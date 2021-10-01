package liquibase.ext.bigquery.sqlgenerator;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.sqlgenerator.core.DeleteGenerator;
import liquibase.statement.core.DeleteStatement;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;

import static liquibase.util.SqlUtil.replacePredicatePlaceholders;

public class BigqueryDeleteGenerator extends DeleteGenerator {

    @Override
    public Sql[] generateSql(DeleteStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sql = new StringBuilder("DELETE FROM ")
            .append(database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()));

        if (statement.getWhere() != null) {
            sql.append(" WHERE ").append(replacePredicatePlaceholders(database, statement.getWhere(), statement.getWhereColumnNames(), statement.getWhereParameters()));
        }else {
            sql.append(" WHERE 1=1");
        }

        return new Sql[] { new UnparsedSql(sql.toString(), getAffectedTable(statement)) };
    }

}
