package liquibase.ext.bigquery.sqlgenerator;

import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.ext.bigquery.database.BigqueryDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.ModifyDataTypeGenerator;
import liquibase.statement.core.ModifyDataTypeStatement;

public class BigQueryModifyDataTypeGenerator extends ModifyDataTypeGenerator {

    @Override
    public boolean supports(ModifyDataTypeStatement statement, Database database) {
        return database instanceof BigqueryDatabase;
    }

    @Override
    public Sql[] generateSql(ModifyDataTypeStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String alterTable;

        alterTable = "ALTER TABLE " + database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName()) +
                " ALTER COLUMN " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) +
                " SET DATA TYPE " + DataTypeFactory.getInstance().fromDescription(statement.getNewDataType(), database).toDatabaseDataType(database);

        return new Sql[]{new UnparsedSql(alterTable, getAffectedTable(statement))};

    }
}
