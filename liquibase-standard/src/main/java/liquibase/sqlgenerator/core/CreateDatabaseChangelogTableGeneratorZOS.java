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
            String catalog   = database.getLiquibaseCatalogName();
            String schema  = database.getLiquibaseSchemaName();
            String tableName = database.escapeTableName(catalog, schema, database.getDatabaseChangeLogTableName());
            
            String dbName = Db2zConfiguration.DATABASECHANGELOG_DATABASE.getCurrentValue();
            String tsName = Db2zConfiguration.DATABASECHANGELOG_TABLESPACE.getCurrentValue();
            boolean hasDb = StringUtils.isNotEmpty(dbName);
            boolean hasTs = StringUtils.isNotEmpty(tsName);

            StringBuilder createTable = new StringBuilder()
               .append("CREATE TABLE ").append(tableName).append(" (")
               .append("ID VARCHAR(255) NOT NULL, ")
               .append("AUTHOR VARCHAR(255) NOT NULL, ")
               .append("FILENAME VARCHAR(255) NOT NULL, ")
               .append("DATEEXECUTED TIMESTAMP NOT NULL, ")
               .append("ORDEREXECUTED INTEGER NOT NULL, ")
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

            if (hasDb && hasTs) {
                createTable.append(" IN ").append(dbName).append(".").append(tsName);
            } else if (hasTs) {
                createTable.append(" IN ").append(tsName);
            } else if (hasDb) {
                createTable.append(" IN ").append(dbName);
            }
            
            Sql createTableSql = new UnparsedSql(createTable.toString(), getAffectedTable(database));

            String idxName = Db2zConfiguration.DATABASECHANGELOG_INDEX.getCurrentValue();
            if (StringUtils.isEmpty(idxName)) {
                idxName = database.getDatabaseChangeLogTableName() + "_PK";
            }
            String qualifiedIdxName = database.escapeObjectName(idxName, Table.class);

                StringBuilder createIndex = new StringBuilder()
                        .append("CREATE UNIQUE INDEX ").append(qualifiedIdxName)
                        .append(" ON ");
                        
                if (schema != null && !schema.isEmpty()) {
                    createIndex.append(schema).append(".");
                }
                
                createIndex.append(database.getDatabaseChangeLogTableName())
                        .append(" (ID, AUTHOR, FILENAME)");

                Sql createIndexSql = new UnparsedSql(createIndex.toString(), getAffectedTable(database));

                return new Sql[] { createTableSql, createIndexSql };

        } finally {
            database.setObjectQuotingStrategy(currentStrategy);
        }
    }
    
    protected Relation getAffectedTable(Database database) {
        return new Table().setName(database.getDatabaseChangeLogTableName())
                         .setSchema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName());
    }
}