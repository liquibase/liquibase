package liquibase.sqlgenerator.core;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.core.Db2zConfiguration;
import liquibase.database.core.Db2zDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateDatabaseChangeLogLockTableStatement;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;
import org.apache.commons.lang3.StringUtils;

/**
 * Creates the DATABASECHANGELOGLOCK table for DB2 on z/OS.
 * This implementation allows specifying DATABASE, TABLESPACE, and INDEX values.
 */
public class CreateDatabaseChangeLogLockTableGeneratorZOS extends AbstractSqlGenerator<CreateDatabaseChangeLogLockTableStatement> {
    
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }
    
    @Override
    public boolean supports(CreateDatabaseChangeLogLockTableStatement statement, Database database) {
        return database instanceof Db2zDatabase;
    }
    
    @Override
    public ValidationErrors validate(CreateDatabaseChangeLogLockTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }
    
    @Override
    public Sql[] generateSql(CreateDatabaseChangeLogLockTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ObjectQuotingStrategy currentStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        
        try {
            // Get the table name
            String tableName = database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName());
            
            // Build the SQL for creating the table
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE ").append(tableName).append(" (")
               .append("ID INT NOT NULL, ")
               .append("LOCKED SMALLINT NOT NULL, ")
               .append("LOCKGRANTED TIMESTAMP, ")
               .append("LOCKEDBY VARCHAR(255), ")
               .append("PRIMARY KEY (ID))");
            
            // Add DATABASE parameter if specified
            String databaseParam = Db2zConfiguration.DATABASECHANGELOGLOCK_DATABASE.getCurrentValue();
            if (StringUtils.isNotEmpty(databaseParam)) {
                sql.append(" IN ").append(database.escapeObjectName(databaseParam, Catalog.class));
            }
            
            // Add TABLESPACE parameter if specified
            String tablespaceParam = Db2zConfiguration.DATABASECHANGELOGLOCK_TABLESPACE.getCurrentValue();
            if (StringUtils.isNotEmpty(tablespaceParam)) {
                sql.append(" TABLESPACE ").append(database.escapeObjectName(tablespaceParam, Table.class));
            }
            
            Sql createTableSql = new UnparsedSql(sql.toString(), getAffectedTable(database));
            
            // Create index if specified
            String indexName = Db2zConfiguration.DATABASECHANGELOGLOCK_INDEX.getCurrentValue();
            if (StringUtils.isNotEmpty(indexName)) {
                String createIndexSql = "CREATE INDEX " + database.escapeObjectName(indexName, Table.class) + 
                                       " ON " + tableName + "(ID)";
                
                return new Sql[] { createTableSql, new UnparsedSql(createIndexSql, getAffectedTable(database)) };
            }
            
            return new Sql[] { createTableSql };
        } finally {
            database.setObjectQuotingStrategy(currentStrategy);
        }
    }
    
    protected Relation getAffectedTable(Database database) {
        return new Table().setName(database.getDatabaseChangeLogLockTableName())
                         .setSchema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName());
    }
}