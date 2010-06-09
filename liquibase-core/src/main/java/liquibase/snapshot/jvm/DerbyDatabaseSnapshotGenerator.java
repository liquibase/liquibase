package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.exception.DatabaseException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DerbyDatabaseSnapshotGenerator extends JdbcDatabaseSnapshotGenerator {
    public boolean supports(Database database) {
        return database instanceof DerbyDatabase;
    }

    public int getPriority(Database database) {
        return PRIORITY_DATABASE;
    }

    @Override
    protected String convertTableNameToDatabaseTableName(String tableName) {
        return tableName.toUpperCase();
    }

    @Override
    protected String convertColumnNameToDatabaseTableName(String columnName) {
        return columnName.toUpperCase();
    }

    /**
     * Derby seems to have troubles
     */
    public boolean hasIndex(String schemaName, String tableName, String indexName, Database database) throws DatabaseException {
        try {
            ResultSet rs = getMetaData(database).getIndexInfo(database.convertRequestedSchemaToCatalog(schemaName), database.convertRequestedSchemaToSchema(schemaName), "%", false, true);
            while (rs.next()) {
                if (rs.getString("INDEX_NAME").equalsIgnoreCase(indexName)) {
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

}