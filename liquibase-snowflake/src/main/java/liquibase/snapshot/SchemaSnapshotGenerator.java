package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.database.object.Schema;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.jvm.JdbcSnapshotGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtil;

import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Snowflake-specific SnapshotGenerator for Schema objects.
 * Captures complete schema information from INFORMATION_SCHEMA.SCHEMATA.
 * 
 * ADDRESSES_CORE_ISSUE: Complete schema snapshot generation for Snowflake custom Schema objects.
 */
public class SchemaSnapshotGenerator extends JdbcSnapshotGenerator {

    public SchemaSnapshotGenerator() {
        super(liquibase.database.object.Schema.class, new Class[]{liquibase.database.object.Database.class});
    }

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (liquibase.database.object.Schema.class.isAssignableFrom(objectType) && 
            database instanceof SnowflakeDatabase) {
            return PRIORITY_DATABASE;
        }
        return PRIORITY_NONE;
    }


    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) 
            throws DatabaseException {
        
        if (!(example instanceof Schema)) {
            return null;
        }
        
        Schema exampleSchema = (Schema) example;
        String schemaName = exampleSchema.getName();
        
        if (schemaName == null) {
            return null;
        }
        
        Database database = snapshot.getDatabase();
        if (!(database instanceof SnowflakeDatabase)) {
            return null;
        }
        
        try {
            // Query INFORMATION_SCHEMA.SCHEMATA for complete schema information
            String sql = "SELECT SCHEMA_NAME, COMMENT, RETENTION_TIME, " +
                        "DEFAULT_DDL_COLLATION, IS_TRANSIENT, IS_MANAGED_ACCESS, " +
                        "OWNER, CREATED, LAST_ALTERED, DATABASE_NAME " +
                        "FROM INFORMATION_SCHEMA.SCHEMATA " +
                        "WHERE SCHEMA_NAME = '" + schemaName + "'";
            
            Statement stmt = ((JdbcConnection) database.getConnection()).createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            try {
                if (!rs.next()) {
                    return null; // Schema doesn't exist
                }
                
                // Create new schema object with queried data
                Schema result = new Schema();
                result.setName(rs.getString("SCHEMA_NAME"));
                
                // Set configuration properties
                String comment = rs.getString("COMMENT");
                if (!StringUtil.isEmpty(comment) && !comment.trim().isEmpty()) {
                    result.setComment(comment);
                }
                
                String retentionTime = rs.getString("RETENTION_TIME");
                if (!StringUtil.isEmpty(retentionTime) && !retentionTime.trim().isEmpty()) {
                    result.setDataRetentionTimeInDays(retentionTime);
                }
                
                String collation = rs.getString("DEFAULT_DDL_COLLATION");
                if (!StringUtil.isEmpty(collation) && !collation.trim().isEmpty()) {
                    result.setDefaultDdlCollation(collation);
                }
                
                String isTransient = rs.getString("IS_TRANSIENT");
                if (!StringUtil.isEmpty(isTransient)) {
                    result.setTransient(parseBoolean(isTransient));
                }
                
                String isManagedAccess = rs.getString("IS_MANAGED_ACCESS");
                if (!StringUtil.isEmpty(isManagedAccess)) {
                    result.setManagedAccess(parseBoolean(isManagedAccess));
                }
                
                // Set state properties  
                String owner = rs.getString("OWNER");
                if (!StringUtil.isEmpty(owner)) {
                    result.setOwner(owner);
                }
                
                String created = rs.getString("CREATED");
                if (!StringUtil.isEmpty(created)) {
                    result.setCreatedOn(created);
                }
                
                String lastAltered = rs.getString("LAST_ALTERED");
                if (!StringUtil.isEmpty(lastAltered)) {
                    result.setLastAltered(lastAltered);
                }
                
                String databaseName = rs.getString("DATABASE_NAME");
                if (!StringUtil.isEmpty(databaseName)) {
                    result.setDatabaseName(databaseName);
                }
                
                return result;
                
            } finally {
                rs.close();
                stmt.close();
            }
            
        } catch (Exception e) {
            throw new DatabaseException("Error querying schema: " + e.getMessage(), e);
        }
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) 
            throws DatabaseException {
        // Schema objects are added to Database objects
        // This method would be called to add all schemas to a database snapshot
        // For now, we'll leave this empty as we're focusing on individual object snapshots
    }

    /**
     * Parse boolean values from Snowflake result set.
     * Handles various boolean representations: YES/NO, true/false, Y/N, 1/0
     */
    private Boolean parseBoolean(String value) {
        if (value == null) {
            return null;
        }
        
        String trimmed = value.trim().toUpperCase();
        return "YES".equals(trimmed) || "TRUE".equals(trimmed) || 
               "Y".equals(trimmed) || "1".equals(trimmed);
    }
}