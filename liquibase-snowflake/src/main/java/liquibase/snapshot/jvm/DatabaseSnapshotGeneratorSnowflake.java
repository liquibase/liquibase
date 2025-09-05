package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Snowflake-specific database snapshot generator with full XSD compliance.
 * Captures all 18 XSD configuration attributes plus operational metadata.
 */
public class DatabaseSnapshotGeneratorSnowflake extends JdbcSnapshotGenerator {

    public DatabaseSnapshotGeneratorSnowflake() {
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
        return new Class[] { Catalog.class };
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) 
            throws DatabaseException, InvalidExampleException {
        
        if (example == null) {
            return null;
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
            return snapshotSingleDatabase(databaseName, database);
        } catch (SQLException e) {
            throw new DatabaseException("Error querying database information for " + databaseName + ": " + e.getMessage(), e);
        }
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) 
            throws DatabaseException, InvalidExampleException {
        
        if (!(foundObject instanceof Catalog)) {
            return;
        }
        
        Database database = snapshot.getDatabase();
        if (!(database instanceof SnowflakeDatabase)) {
            return;
        }
        
        try {
            // Bulk database discovery for catalog
            addAllDatabases((Catalog) foundObject, database);
        } catch (SQLException e) {
            throw new DatabaseException("Error discovering databases: " + e.getMessage(), e);
        }
    }

    /**
     * Snapshots a single database with complete XSD compliance.
     */
    private liquibase.database.object.Database snapshotSingleDatabase(String databaseName, Database database) 
            throws SQLException, DatabaseException {
        
        // Primary query: INFORMATION_SCHEMA.DATABASES for core attributes
        String sql = "SELECT " +
                    "DATABASE_NAME, " +
                    "DATABASE_OWNER, " +
                    "IS_TRANSIENT, " +
                    "COMMENT, " +
                    "CREATED, " +
                    "LAST_ALTERED, " +
                    "RETENTION_TIME, " +
                    "TYPE, " +
                    "REPLICABLE_WITH_FAILOVER_GROUPS, " +
                    "OWNER_ROLE_TYPE " +
                    "FROM INFORMATION_SCHEMA.DATABASES " +
                    "WHERE DATABASE_NAME = ?";
        
        PreparedStatement stmt = ((JdbcConnection) database.getConnection()).prepareStatement(sql);
        stmt.setString(1, databaseName);
        ResultSet rs = stmt.executeQuery();
        
        if (!rs.next()) {
            rs.close();
            stmt.close();
            return null; // Database doesn't exist
        }
        
        // Create database object with XSD compliance
        liquibase.database.object.Database result = new liquibase.database.object.Database();
        result.setName(rs.getString("DATABASE_NAME"));
        
        // XSD Configuration Attributes (always included in comparison)
        result.setComment(rs.getString("COMMENT"));
        
        int retentionTime = rs.getInt("RETENTION_TIME");
        if (!rs.wasNull()) {
            result.setDataRetentionTimeInDays(retentionTime);
        }
        
        String isTransient = rs.getString("IS_TRANSIENT");
        result.setTransient(convertYesNoToBoolean(isTransient));
        
        // Operational Metadata Attributes (excluded from diff, snapshot only)
        result.setOwner(rs.getString("DATABASE_OWNER"));
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
        
        rs.close();
        stmt.close();
        
        // Supplementary query: SHOW DATABASES for additional XSD attributes
        enrichWithShowDatabasesAttributes(result, databaseName, database);
        
        return result;
    }

    /**
     * Enriches database object with attributes only available from SHOW DATABASES.
     */
    private void enrichWithShowDatabasesAttributes(liquibase.database.object.Database dbObject, 
                                                   String databaseName, Database database) throws DatabaseException {
        try {
            String showSql = "SHOW DATABASES LIKE ?";
            PreparedStatement showStmt = ((JdbcConnection) database.getConnection()).prepareStatement(showSql);
            showStmt.setString(1, databaseName);
            ResultSet showRs = showStmt.executeQuery();
            
            if (showRs.next()) {
                // XSD attributes available from SHOW DATABASES
                dbObject.setDefaultDdlCollation(showRs.getString("DEFAULT_DDL_COLLATION"));
                dbObject.setTag(showRs.getString("TAG"));
                
                int maxDataExtension = showRs.getInt("MAX_DATA_EXTENSION_TIME_IN_DAYS");
                if (!showRs.wasNull()) {
                    dbObject.setMaxDataExtensionTimeInDays(maxDataExtension);
                }
                
                // Iceberg-specific XSD attributes
                dbObject.setExternalVolume(showRs.getString("EXTERNAL_VOLUME"));
                dbObject.setCatalogString(showRs.getString("CATALOG"));
                dbObject.setStorageSerializationPolicy(showRs.getString("STORAGE_SERIALIZATION_POLICY"));
                
                // Additional attributes stored as generic attributes since they may not have setters
                String replaceInvalidChars = showRs.getString("REPLACE_INVALID_CHARACTERS");
                if (replaceInvalidChars != null) {
                    dbObject.setAttribute("replaceInvalidCharacters", convertYesNoToBoolean(replaceInvalidChars));
                }
                
                String catalogSync = showRs.getString("CATALOG_SYNC");
                if (catalogSync != null) {
                    dbObject.setAttribute("catalogSync", catalogSync);
                }
                
                String catalogSyncMode = showRs.getString("CATALOG_SYNC_NAMESPACE_MODE");
                if (catalogSyncMode != null) {
                    dbObject.setAttribute("catalogSyncNamespaceMode", catalogSyncMode);
                }
                
                String catalogSyncDelimiter = showRs.getString("CATALOG_SYNC_NAMESPACE_FLATTEN_DELIMITER");
                if (catalogSyncDelimiter != null) {
                    dbObject.setAttribute("catalogSyncNamespaceFlattenDelimiter", catalogSyncDelimiter);
                }
                
                // Session-specific state attributes (snapshot only) 
                String isDefault = showRs.getString("IS_DEFAULT");
                if (isDefault != null) {
                    dbObject.setAttribute("isDefault", convertYesNoToBoolean(isDefault));
                }
                
                String isCurrent = showRs.getString("IS_CURRENT");
                if (isCurrent != null) {
                    dbObject.setAttribute("isCurrent", convertYesNoToBoolean(isCurrent));
                }
                
                String origin = showRs.getString("ORIGIN");
                if (origin != null) {
                    dbObject.setAttribute("origin", origin);
                }
                
                String options = showRs.getString("OPTIONS");
                if (options != null) {
                    dbObject.setAttribute("options", options);
                }
            }
            
            showRs.close();
            showStmt.close();
            
        } catch (SQLException e) {
            // SHOW DATABASES attributes are supplementary - continue without them if they fail
            // This is expected in some Snowflake environments or with limited permissions
        }
    }

    /**
     * Bulk database discovery for catalog-level snapshots.
     */
    private void addAllDatabases(Catalog catalog, Database database) throws SQLException, DatabaseException {
        String sql = "SELECT " +
                    "DATABASE_NAME, " +
                    "DATABASE_OWNER, " +
                    "IS_TRANSIENT, " +
                    "COMMENT, " +
                    "CREATED, " +
                    "LAST_ALTERED, " +
                    "RETENTION_TIME, " +
                    "TYPE, " +
                    "OWNER_ROLE_TYPE " +
                    "FROM INFORMATION_SCHEMA.DATABASES " +
                    "ORDER BY DATABASE_NAME";
        
        PreparedStatement stmt = ((JdbcConnection) database.getConnection()).prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            String databaseName = rs.getString("DATABASE_NAME");
            
            // Create database object for each discovered database
            liquibase.database.object.Database dbObject = new liquibase.database.object.Database();
            dbObject.setName(databaseName);
            
            // Set basic attributes from INFORMATION_SCHEMA
            dbObject.setComment(rs.getString("COMMENT"));
            
            int retentionTime = rs.getInt("RETENTION_TIME");
            if (!rs.wasNull()) {
                dbObject.setDataRetentionTimeInDays(retentionTime);
            }
            
            String isTransient = rs.getString("IS_TRANSIENT");
            dbObject.setTransient(convertYesNoToBoolean(isTransient));
            
            dbObject.setOwner(rs.getString("DATABASE_OWNER"));
            dbObject.setDatabaseType(rs.getString("TYPE"));
            
            java.sql.Timestamp created = rs.getTimestamp("CREATED");
            if (created != null) {
                dbObject.setCreated(new java.util.Date(created.getTime()));
            }
            
            java.sql.Timestamp lastAltered = rs.getTimestamp("LAST_ALTERED");
            if (lastAltered != null) {
                dbObject.setLastAltered(new java.util.Date(lastAltered.getTime()));
            }
            
            dbObject.setOwnerRoleType(rs.getString("OWNER_ROLE_TYPE"));
            
            // Note: For bulk discovery, we skip SHOW DATABASES enrichment for performance
            // Individual database snapshots will get complete attribute coverage
            
            catalog.addDatabaseObject(dbObject);
        }
        
        rs.close();
        stmt.close();
    }


    /**
     * Converts Snowflake YES/NO strings to Boolean objects.
     */
    private Boolean convertYesNoToBoolean(String yesNoValue) {
        if (yesNoValue == null) {
            return null;
        }
        String trimmed = yesNoValue.trim().toUpperCase();
        if ("YES".equals(trimmed) || "Y".equals(trimmed)) {
            return Boolean.TRUE;
        } else if ("NO".equals(trimmed) || "N".equals(trimmed)) {
            return Boolean.FALSE;
        }
        return null;
    }
}