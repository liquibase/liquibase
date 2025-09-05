package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.jvm.JdbcSnapshotGenerator;
import liquibase.structure.DatabaseObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Snowflake-specific database snapshot generator.
 * Captures database objects and their configuration from Snowflake.
 */
public class DatabaseSnapshotGenerator extends JdbcSnapshotGenerator {

    public DatabaseSnapshotGenerator() {
        super(liquibase.database.object.Database.class, new Class[]{});
    }

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (liquibase.database.object.Database.class.isAssignableFrom(objectType) && 
            database instanceof SnowflakeDatabase) {
            return PRIORITY_DATABASE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] addsTo() {
        return new Class[0];
    }

    @Override
    public Class<? extends SnapshotGenerator>[] replaces() {
        return null;
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) 
            throws DatabaseException, InvalidExampleException {
        
        if (example == null) {
            throw new NullPointerException("Example database object cannot be null");
        }
        
        if (snapshot == null) {
            throw new NullPointerException("Database snapshot cannot be null");
        }
        
        if (!(example instanceof liquibase.database.object.Database)) {
            return null;
        }
        
        liquibase.database.object.Database exampleDatabase = 
            (liquibase.database.object.Database) example;
        String databaseName = exampleDatabase.getName();
        
        if (databaseName == null) {
            return null;
        }
        
        Database database = snapshot.getDatabase();
        if (!(database instanceof SnowflakeDatabase)) {
            return null;
        }
        
        try {
            // Query INFORMATION_SCHEMA.DATABASES for basic information
            String sql = "SELECT DATABASE_NAME, COMMENT, RETENTION_TIME, " +
                        "IS_TRANSIENT, DEFAULT_DDL_COLLATION, OWNER, TYPE, " +
                        "CREATED, LAST_ALTERED, OWNER_ROLE_TYPE " +
                        "FROM INFORMATION_SCHEMA.DATABASES " +
                        "WHERE DATABASE_NAME = '" + databaseName + "'";
            
            Statement stmt = ((JdbcConnection) database.getConnection()).createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            if (!rs.next()) {
                return null; // Database doesn't exist
            }
            
            // Create new database object with queried data
            liquibase.database.object.Database result = 
                new liquibase.database.object.Database();
            result.setName(rs.getString("DATABASE_NAME"));
            
            // Set configuration properties
            String comment = rs.getString("COMMENT");
            if (comment != null && !comment.trim().isEmpty()) {
                result.setComment(comment);
            }
            
            int retentionTime = rs.getInt("RETENTION_TIME");
            if (!rs.wasNull()) {
                result.setDataRetentionTimeInDays(retentionTime);
            }
            
            String isTransient = rs.getString("IS_TRANSIENT");
            if (isTransient != null) {
                result.setTransient("YES".equalsIgnoreCase(isTransient));
            }
            
            String defaultDdlCollation = rs.getString("DEFAULT_DDL_COLLATION");
            if (defaultDdlCollation != null && !defaultDdlCollation.trim().isEmpty()) {
                result.setDefaultDdlCollation(defaultDdlCollation);
            }
            
            // Set operational state properties
            result.setOwner(rs.getString("OWNER"));
            result.setDatabaseType(rs.getString("TYPE"));
            
            java.sql.Timestamp created = rs.getTimestamp("CREATED");
            if (created != null) {
                result.setCreated(new java.util.Date(created.getTime()));
            }
            
            java.sql.Timestamp lastAltered = rs.getTimestamp("LAST_ALTERED");
            if (lastAltered != null) {
                result.setLastAltered(new java.util.Date(lastAltered.getTime()));
            }
            
            result.setOwnerRoleType(rs.getString("OWNER_ROLE_TYPE"));
            
            // Close resources
            rs.close();
            stmt.close();
            
            // Try to get Iceberg attributes from SHOW DATABASES if available
            try {
                String showSql = "SHOW DATABASES LIKE '" + databaseName + "'";
                Statement showStmt = ((JdbcConnection) database.getConnection()).createStatement();
                ResultSet showRs = showStmt.executeQuery(showSql);
                
                if (showRs.next()) {
                    String externalVolume = showRs.getString("EXTERNAL_VOLUME");
                    if (externalVolume != null && !externalVolume.trim().isEmpty()) {
                        result.setExternalVolume(externalVolume);
                    }
                    
                    String catalog = showRs.getString("CATALOG");
                    if (catalog != null && !catalog.trim().isEmpty()) {
                        result.setCatalogString(catalog);
                    }
                    
                    String storagePolicy = showRs.getString("STORAGE_SERIALIZATION_POLICY");
                    if (storagePolicy != null && !storagePolicy.trim().isEmpty()) {
                        result.setStorageSerializationPolicy(storagePolicy);
                    }
                }
                
                showRs.close();
                showStmt.close();
                
            } catch (SQLException e) {
                // Iceberg attributes may not be available - continue without them
            }
            
            return result;
            
        } catch (SQLException e) {
            throw new DatabaseException("Error querying database information: " + e.getMessage(), e);
        }
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) 
            throws DatabaseException, InvalidExampleException {
        // Databases are top-level objects, no need to add to other objects
    }
}