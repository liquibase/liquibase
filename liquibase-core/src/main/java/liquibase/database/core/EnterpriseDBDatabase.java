package liquibase.database.core;

import liquibase.database.DatabaseConnection;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.DatabaseException;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.util.StringUtil;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.structure.DatabaseObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EnterpriseDBDatabase extends PostgresDatabase {

    private static final int HIGH_PRIORITY = 5;

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return StringUtil.trimToEmpty(System.getProperty("liquibase.ext.postgresedb.force")).equalsIgnoreCase("true")
                || conn.getURL().contains("edb")
                || conn.getURL().contains(":5444");
    }

    @Override
    public String getShortName() {
        return "edb";
    }

    // @Override
    // protected String getDefaultDatabaseProductName() {
    //     return "PostgresEDB";
    // }

    @Override
    public int getPriority() {
        return HIGH_PRIORITY;
    }


    // @Override
    // public String escapeObjectName(String objectName, final Class<? extends DatabaseObject> objectType) {
    //     if (objectName != null) {
    //         objectName = objectName.trim();
    //         if (mustQuoteObjectName(objectName, objectType)) {
    //             return quoteObject(objectName, objectType);
    //         } else if (quotingStrategy == ObjectQuotingStrategy.QUOTE_ALL_OBJECTS) {
    //             return quoteObject(objectName, objectType);
    //         }
    //     }
    //     return objectName;
    // }

    @Override
    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:edb:")) {
            return "com.edb.Driver";
        }
        return null;
    }

    @Override
    public Integer getDefaultPort() {
        return 5444;
    }

    
    @Override
    protected SqlStatement getConnectionSchemaNameCallStatement() {
        return new RawSqlStatement("select current_schema()");
    }

}
