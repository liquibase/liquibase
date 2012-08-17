package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.structure.Schema;
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

    /**
     * Derby seems to have troubles
     */
    @Override
    public boolean hasIndex(Schema schema, String tableName, String indexName, String columnNames, Database database) throws DatabaseException {
        try {
            ResultSet rs = getMetaData(database).getIndexInfo(schema.getCatalogName(), schema.getName(), "%", false, true);
            while (rs.next()) {
                if (rs.getString("INDEX_NAME").equalsIgnoreCase(indexName)) {
                    return true;
                }
                if (tableName != null && columnNames != null) {
                    if (tableName.equalsIgnoreCase(rs.getString("TABLE_NAME")) && columnNames.replaceAll(" ","").equalsIgnoreCase(rs.getString("COLUMN_NAME").replaceAll(" ",""))) {
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

}