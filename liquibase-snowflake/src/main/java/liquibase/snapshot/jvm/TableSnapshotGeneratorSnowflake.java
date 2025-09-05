package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.util.StringUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Snowflake-specific snapshot generator for Table objects.
 * Captures Snowflake-specific table attributes like clustering keys, retention time, and transient flag.
 * 
 * ADDRESSES_CORE_ISSUE: Provides enhanced Table snapshot functionality for Snowflake-specific attributes.
 */
public class TableSnapshotGeneratorSnowflake extends JdbcSnapshotGenerator {

    public TableSnapshotGeneratorSnowflake() {
        super(Table.class, new Class[]{Schema.class});
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException {
        Database database = snapshot.getDatabase();
        
        // Only handle Snowflake databases
        if (!(database instanceof SnowflakeDatabase)) {
            return null;
        }
        
        Table table = (Table) example;
        
        // Validate table has name
        if (table.getName() == null) {
            return null;
        }
        
        Statement statement = null;
        ResultSet resultSet = null;
        
        try {
            statement = ((JdbcConnection) database.getConnection()).createStatement();
            
            // Build query to get Snowflake-specific table information
            String query = buildTableQuery(table, database);
            resultSet = statement.executeQuery(query);
            
            if (resultSet.next()) {
                // Create result table object
                Table result = new Table();
                result.setName(resultSet.getString("TABLE_NAME"));
                
                // Set schema
                String schemaName = resultSet.getString("TABLE_SCHEMA");
                if (schemaName != null) {
                    result.setSchema(new liquibase.structure.core.Schema(table.getSchema().getCatalogName(), schemaName));
                }
                
                // Set standard table attributes
                result.setRemarks(resultSet.getString("COMMENT"));
                
                // Set Snowflake-specific attributes
                setTableAttribute(result, "clusteringKey", resultSet.getString("CLUSTERING_KEY"));
                setTableAttribute(result, "retentionTime", resultSet.getString("RETENTION_TIME"));
                setTableAttribute(result, "isTransient", resultSet.getString("IS_TRANSIENT"));
                setTableAttribute(result, "created", resultSet.getString("CREATED"));
                setTableAttribute(result, "lastAltered", resultSet.getString("LAST_ALTERED"));
                setTableAttribute(result, "owner", resultSet.getString("TABLE_OWNER"));
                setTableAttribute(result, "rowCount", resultSet.getString("ROW_COUNT"));
                setTableAttribute(result, "bytes", resultSet.getString("BYTES"));
                
                return result;
            }
            
            return null;
            
        } catch (SQLException e) {
            throw new DatabaseException("Error snapshotting table " + table.getName(), e);
        } catch (Exception e) {
            throw new DatabaseException("Error snapshotting table " + table.getName(), e);
        } finally {
            // Cleanup resources
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    // Log but don't throw
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    // Log but don't throw
                }
            }
        }
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException {
        // No additional processing needed for tables
    }

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Table.class.isAssignableFrom(objectType) && database instanceof SnowflakeDatabase) {
            return PRIORITY_DATABASE;
        }
        return PRIORITY_NONE;
    }

    /**
     * Builds the SQL query to retrieve Snowflake-specific table information.
     */
    private String buildTableQuery(Table table, Database database) {
        String schemaName = table.getSchema() != null ? table.getSchema().getName() : "PUBLIC";
        String tableName = table.getName();
        
        return "SELECT " +
               "    TABLE_NAME, " +
               "    TABLE_SCHEMA, " +
               "    TABLE_TYPE, " +
               "    COMMENT, " +
               "    CLUSTERING_KEY, " +
               "    RETENTION_TIME, " +
               "    IS_TRANSIENT, " +
               "    CREATED, " +
               "    LAST_ALTERED, " +
               "    TABLE_OWNER, " +
               "    ROW_COUNT, " +
               "    BYTES " +
               "FROM INFORMATION_SCHEMA.TABLES " +
               "WHERE TABLE_SCHEMA = '" + schemaName + "' " +
               "  AND TABLE_NAME = '" + tableName + "'";
    }

    /**
     * Sets a table attribute if the value is not null, empty, or whitespace-only.
     * Follows the same pattern as SchemaSnapshotGenerator.
     */
    private void setTableAttribute(Table table, String attributeName, String value) {
        if (!StringUtil.isEmpty(value) && !value.trim().isEmpty()) {
            table.setAttribute(attributeName, value);
        }
    }
}