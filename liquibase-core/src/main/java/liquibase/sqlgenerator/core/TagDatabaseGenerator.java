package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.TagDatabaseStatement;
import liquibase.statement.core.UpdateStatement;

public class TagDatabaseGenerator extends AbstractSqlGenerator<TagDatabaseStatement> {

    @Override
    public ValidationErrors validate(TagDatabaseStatement tagDatabaseStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tag", tagDatabaseStatement.getTag());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(TagDatabaseStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
    	String liquibaseSchema = null;
   		liquibaseSchema = database.getLiquibaseSchemaName();
   		String escapedTableName = database.escapeTableName(database.getLiquibaseCatalogName(), liquibaseSchema, database.getDatabaseChangeLogTableName());

   		UpdateStatement updateStatement = new UpdateStatement(database.getLiquibaseCatalogName(), liquibaseSchema, database.getDatabaseChangeLogTableName());
        updateStatement.addNewColumnValue("TAG", statement.getTag());
		if (database instanceof MySQLDatabase) {
            try {
                long version = Long.parseLong(database.getDatabaseProductVersion().substring(0, 1));

                if (version < 5) {
                    return new Sql[]{
                            new UnparsedSql("UPDATE "+escapedTableName+" C LEFT JOIN (SELECT MAX(DATEEXECUTED) as MAXDATE FROM (SELECT DATEEXECUTED FROM `DATABASECHANGELOG`) AS X) D ON C.DATEEXECUTED = D.MAXDATE SET C.TAG = '" + statement.getTag() + "' WHERE D.MAXDATE IS NOT NULL")
                    };
                }

            } catch (Throwable e) {
                ; //assume it is version 5
            }
            updateStatement.setWhereClause("ORDEREXECUTED > (SELECT oe FROM (" +
            		"   SELECT" +
            		"   	CASE" +
            		"   		WHEN max(ORDEREXECUTED) IS NULL THEN 0" +
            		"   		ELSE max(ORDEREXECUTED)" +
            		"   	END as oe" +
            		"   FROM "  + escapedTableName + " WHERE TAG IS NOT NULL) AS X)");
        } else if (database instanceof InformixDatabase) {
            return new Sql[]{
                    new UnparsedSql(
                    		"   SELECT" +
                    		"   	CASE" +
                    		"   		WHEN max(ORDEREXECUTED) IS NULL THEN 0" +
                    		"   		ELSE max(ORDEREXECUTED)" +
                    		"   	END as oe" +
                    		"   FROM "  + escapedTableName + " WHERE TAG IS NOT NULL INTO TEMP oe_temp WITH NO LOG"),
                    new UnparsedSql("UPDATE "+escapedTableName+" SET TAG = '"+statement.getTag()+"' WHERE ORDEREXECUTED >  (SELECT oe FROM oe_temp)"),
                    new UnparsedSql("DROP TABLE oe_temp")
            };
        } else {
            updateStatement.setWhereClause("ORDEREXECUTED > (SELECT oe FROM (" +
            		"   SELECT" +
            		"   	CASE" +
            		"   		WHEN max(ORDEREXECUTED) IS NULL THEN 0" +
            		"   		ELSE max(ORDEREXECUTED)" +
            		"   	END as oe" +
            		"   FROM "  + escapedTableName + " WHERE TAG IS NOT NULL) AS X)");
        }

        return SqlGeneratorFactory.getInstance().generateSql(updateStatement, database);

    }
}
