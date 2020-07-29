package liquibase.sqlgenerator.core;

import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.SelectFromDatabaseChangeLogLockStatement;
import liquibase.util.StringUtil;

public class SelectFromDatabaseChangeLogLockGenerator extends AbstractSqlGenerator<SelectFromDatabaseChangeLogLockStatement> {

    @Override
    public ValidationErrors validate(SelectFromDatabaseChangeLogLockStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors errors = new ValidationErrors();
        errors.checkRequiredField("columnToSelect", statement.getColumnsToSelect());

        return errors;
    }

    @Override
    public Sql[] generateSql(SelectFromDatabaseChangeLogLockStatement statement, final Database database, SqlGeneratorChain sqlGeneratorChain) {
    	String liquibaseSchema;
   		liquibaseSchema = database.getLiquibaseSchemaName();
		
		ColumnConfig[] columns = statement.getColumnsToSelect();
		int numberOfColumns = columns.length;
        // use LEGACY quoting since we're dealing with system objects
        ObjectQuotingStrategy currentStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        try {
            String sql = "SELECT " + StringUtil.join(statement.getColumnsToSelect(), ",", new StringUtil.StringUtilFormatter<ColumnConfig>() {
                @Override
                public String toString(ColumnConfig col) {
                    if ((col.getComputed() != null) && col.getComputed()) {
                        return col.getName();
                    } else {
                        return database.escapeColumnName(null, null, null, col.getName());
                    }
                }
            }) + " FROM " +
                    database.escapeTableName(database.getLiquibaseCatalogName(), liquibaseSchema, database.getDatabaseChangeLogLockTableName()) +
                " WHERE " + database.escapeColumnName(database.getLiquibaseCatalogName(), liquibaseSchema, database.getDatabaseChangeLogLockTableName(), "ID") + "=1";

            if (database instanceof OracleDatabase) {
                sql += " FOR UPDATE";
            }
            return new Sql[] {
                    new UnparsedSql(sql)
            };
        } finally {
            database.setObjectQuotingStrategy(currentStrategy);
        }
    }
}
