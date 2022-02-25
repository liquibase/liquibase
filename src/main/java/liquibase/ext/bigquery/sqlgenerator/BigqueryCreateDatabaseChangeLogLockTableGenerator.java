package liquibase.ext.bigquery.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.datatype.DataTypeFactory;
import liquibase.ext.bigquery.database.BigqueryDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.sqlgenerator.core.CreateDatabaseChangeLogLockTableGenerator;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.core.CreateDatabaseChangeLogLockTableStatement;
import liquibase.statement.core.CreateTableStatement;

import static liquibase.ext.bigquery.database.BigqueryDatabase.BIGQUERY_PRIORITY_DATABASE;

public class BigqueryCreateDatabaseChangeLogLockTableGenerator extends CreateDatabaseChangeLogLockTableGenerator {

    @Override
    public int getPriority() {
        return BIGQUERY_PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(CreateDatabaseChangeLogLockTableStatement statement, Database database) {
        return database instanceof BigqueryDatabase;
    }

    @Override
    public Sql[] generateSql(CreateDatabaseChangeLogLockTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String charTypeName = getCharTypeName(database);
        String dateTimeTypeString = getDateTimeTypeString(database);
        CreateTableStatement createTableStatement = new CreateTableStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName())
                .setTablespace(database.getLiquibaseTablespaceName())
                .addColumn("ID", DataTypeFactory.getInstance().fromDescription("int", database), null, null, new NotNullConstraint())
                .addColumn("LOCKED", DataTypeFactory.getInstance().fromDescription("bool", database), null, null, new NotNullConstraint())
                .addColumn("LOCKGRANTED", DataTypeFactory.getInstance().fromDescription(dateTimeTypeString, database))
                .addColumn("LOCKEDBY", DataTypeFactory.getInstance().fromDescription(charTypeName + "(255)", database));

        // use LEGACY quoting since we're dealing with system objects
        ObjectQuotingStrategy currentStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        try {
            return SqlGeneratorFactory.getInstance().generateSql(createTableStatement, database);
        } finally {
            database.setObjectQuotingStrategy(currentStrategy);
        }
    }

    @Override
    protected String getCharTypeName(Database database) {
        return "string";
    }

}
