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
            String catalog = database.getLiquibaseCatalogName();
            String schema = database.getLiquibaseSchemaName();
            String tableName = database.escapeTableName(catalog, schema, database.getDatabaseChangeLogLockTableName());

            String dbName = Db2zConfiguration.DATABASECHANGELOGLOCK_DATABASE.getCurrentValue();
            String tsName = Db2zConfiguration.DATABASECHANGELOGLOCK_TABLESPACE.getCurrentValue();
            boolean hasDb = StringUtils.isNotEmpty(dbName);
            boolean hasTs = StringUtils.isNotEmpty(tsName);

            StringBuilder createTable = new StringBuilder()
                    .append("CREATE TABLE ").append(tableName).append(" (")
                    .append("ID INT NOT NULL, ")
                    .append("LOCKED SMALLINT NOT NULL, ")
                    .append("LOCKGRANTED TIMESTAMP, ")
                    .append("LOCKEDBY VARCHAR(255), ")
                    .append("PRIMARY KEY (ID))");

            if (hasDb && hasTs) {
                createTable.append(" IN ").append(dbName).append(".").append(tsName);
            } else if (hasTs) {
                createTable.append(" IN ").append(tsName);
            } else if (hasDb) {
                createTable.append(" IN ").append(dbName);
            }

            Sql createTableSql = new UnparsedSql(createTable.toString(), getAffectedTable(database));

            String idxName = Db2zConfiguration.DATABASECHANGELOGLOCK_INDEX.getCurrentValue();
            if (StringUtils.isEmpty(idxName)) {
                idxName = database.getDatabaseChangeLogLockTableName() + "_PK";
            }
            String qualifiedIdxName = database.escapeObjectName(idxName, Table.class);

            StringBuilder createIndex = new StringBuilder()
                    .append("CREATE UNIQUE INDEX ").append(qualifiedIdxName)
                    .append(" ON ")
                    .append(schema)
                    .append(".")
                    .append(database.getDatabaseChangeLogLockTableName())
                    .append(" (ID)");

            Sql createIndexSql = new UnparsedSql(createIndex.toString(), getAffectedTable(database));

            return new Sql[]{createTableSql, createIndexSql};

        } finally {
            database.setObjectQuotingStrategy(currentStrategy);
        }
    }
    
    protected Relation getAffectedTable(Database database) {
        return new Table().setName(database.getDatabaseChangeLogLockTableName())
                         .setSchema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName());
    }
}