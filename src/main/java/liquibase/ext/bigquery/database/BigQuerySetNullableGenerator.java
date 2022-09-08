package liquibase.ext.bigquery.database;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeFactory;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.sqlgenerator.core.SetNullableGenerator;
import liquibase.statement.core.ReorganizeTableStatement;
import liquibase.statement.core.SetNullableStatement;
import liquibase.structure.DatabaseObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BigQuerySetNullableGenerator extends SetNullableGenerator {

    @Override
    public boolean supports(SetNullableStatement statement, Database database) {
        if (database.equals(BigqueryDatabase.PRODUCT_NAME)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int getPriority() {
        return BigqueryDatabase.PRIORITY_DATABASE;
    }

    public Sql[] generateSql(SetNullableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String nullableString = statement.isNullable() ? " NULL" : " NOT NULL";
        String sql = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " DROP" + nullableString;

        List<Sql> returnList = new ArrayList();
        returnList.add(new UnparsedSql(sql, new DatabaseObject[]{this.getAffectedColumn(statement)}));
        if (database instanceof DB2Database) {
            Sql[] a = SqlGeneratorFactory.getInstance().generateSql(new ReorganizeTableStatement(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()), database);
            if (a != null) {
                returnList.addAll(Arrays.asList(a));
            }
        }

        return (Sql[])returnList.toArray(new Sql[returnList.size()]);
    }

}
