package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.core.Db2zConfiguration;
import liquibase.database.core.Db2zDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;
import org.apache.commons.lang3.StringUtils;

/**
 * Creates the DATABASECHANGELOG table for DB2 on z/OS.
 * This implementation allows specifying DATABASE, TABLESPACE, and INDEX values.
 */
public class CreateDatabaseChangelogTableGeneratorZOS extends AbstractSqlGenerator<CreateDatabaseChangeLogTableStatement> {
    
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }
    
    @Override
    public boolean supports(CreateDatabaseChangeLogTableStatement statement, Database database) {
        return database instanceof Db2zDatabase;
    }
    
    @Override
    public ValidationErrors validate(CreateDatabaseChangeLogTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }
    
    @Override
    public Sql[] generateSql(CreateDatabaseChangeLogTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ObjectQuotingStrategy currentStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        
        try {
            // Get the table name
            String tableName = database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName());
            
            // Build the SQL for creating the table
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE ").append(tableName).append(" (")
               .append("ID VARCHAR(255) NOT NULL, ")
               .append("AUTHOR VARCHAR(255) NOT NULL, ")
               .append("FILENAME VARCHAR(255) NOT NULL, ")
               .append("DATEEXECUTED TIMESTAMP NOT NULL, ")
               .append("ORDEREXECUTED INT NOT NULL, ")
               .append("EXECTYPE VARCHAR(10) NOT NULL, ")
               .append("MD5SUM VARCHAR(35), ")
               .append("DESCRIPTION VARCHAR(255), ")
               .append("COMMENTS VARCHAR(255), ")
               .append("TAG VARCHAR(255), ")
               .append("LIQUIBASE VARCHAR(20), ")
               .append("CONTEXTS VARCHAR(255), ")
               .append("LABELS VARCHAR(255), ")
               .append("DEPLOYMENT_ID VARCHAR(10), ")
               .append("PRIMARY KEY(ID, AUTHOR, FILENAME))");
            
            // Add DATABASE parameter if specified
            String databaseParam = Db2zConfiguration.DATABASECHANGELOG_DATABASE.getCurrentValue();
            if (StringUtils.isNotEmpty(databaseParam)) {
                sql.append(" IN ").append(database.escapeObjectName(databaseParam, Catalog.class));
            }
            
            // Add TABLESPACE parameter if specified
            String tablespaceParam = Db2zConfiguration.DATABASECHANGELOG_TABLESPACE.getCurrentValue();
            if (StringUtils.isNotEmpty(tablespaceParam)) {
                sql.append(" TABLESPACE ").append(database.escapeObjectName(tablespaceParam, Table.class));
            }
            
            Sql createTableSql = new UnparsedSql(sql.toString(), getAffectedTable(database));
            
            // Create index if specified
            String indexName = Db2zConfiguration.DATABASECHANGELOG_INDEX.getCurrentValue();
            if (StringUtils.isNotEmpty(indexName)) {
                String createIndexSql = "CREATE INDEX " + database.escapeObjectName(indexName, Table.class) + 
                                       " ON " + tableName + "(ID, AUTHOR, FILENAME)";
                
                return new Sql[] { createTableSql, new UnparsedSql(createIndexSql, getAffectedTable(database)) };
            }
            
            return new Sql[] { createTableSql };
        } finally {
            database.setObjectQuotingStrategy(currentStrategy);
        }
    }
    
    protected Relation getAffectedTable(Database database) {
        return new Table().setName(database.getDatabaseChangeLogTableName())
                         .setSchema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName());
    }
}