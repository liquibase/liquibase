package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Snowflake-specific warehouse snapshot generator.
 * Captures warehouse information as schema metadata for snapshot operations.
 */
public class WarehouseSnapshotGeneratorSnowflake extends JdbcSnapshotGenerator {

    public WarehouseSnapshotGeneratorSnowflake() {
        super(Schema.class, new Class[]{});
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        return example;
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!snapshot.getSnapshotControl().shouldInclude(Schema.class)) {
            return;
        }

        if (foundObject instanceof Schema) {
            Schema schema = (Schema) foundObject;
            Database database = snapshot.getDatabase();
            
            if (!(database instanceof SnowflakeDatabase)) {
                return;
            }
            
            try {
                // Capture warehouse information as schema metadata
                StringBuilder warehouseInfo = new StringBuilder();
                
                Statement stmt = ((JdbcConnection) database.getConnection()).createStatement();
                ResultSet rs = stmt.executeQuery("SHOW WAREHOUSES");
                
                while (rs.next()) {
                    String warehouseName = rs.getString("name");
                    String warehouseSize = rs.getString("size");
                    String warehouseState = rs.getString("state");
                    
                    if (warehouseInfo.length() > 0) {
                        warehouseInfo.append("; ");
                    }
                    warehouseInfo.append(warehouseName)
                                 .append("(")
                                 .append(warehouseSize)
                                 .append(",")
                                 .append(warehouseState)
                                 .append(")");
                }
                
                rs.close();
                stmt.close();
                
                // Store warehouse information as a schema attribute for reference
                if (warehouseInfo.length() > 0) {
                    schema.setAttribute("snowflake.warehouses", warehouseInfo.toString());
                }
                
            } catch (SQLException | DatabaseException e) {
                // If we can't query warehouses, continue without this information
                // This might happen due to permission issues
            }
        }
    }

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (database instanceof SnowflakeDatabase && Schema.class.isAssignableFrom(objectType)) {
            return PRIORITY_DATABASE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends SnapshotGenerator>[] replaces() {
        return new Class[0];
    }
}