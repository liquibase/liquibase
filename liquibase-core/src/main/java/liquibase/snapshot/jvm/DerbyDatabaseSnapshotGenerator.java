package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.structure.core.Column;
import liquibase.exception.DatabaseException;

import java.sql.SQLException;
import java.util.Map;

public class DerbyDatabaseSnapshotGenerator extends JdbcDatabaseSnapshotGenerator {
    public boolean supports(Database database) {
        return database instanceof DerbyDatabase;
    }

    public int getPriority(Database database) {
        return PRIORITY_DATABASE;
    }

    @Override
    protected Object readDefaultValue(Map<String, Object> columnMetadataResultSet, Column columnInfo, Database database) throws SQLException, DatabaseException {
        Object val = columnMetadataResultSet.get("COLUMN_DEF");

        if (val instanceof String && "GENERATED_BY_DEFAULT".equals(val)) {
            return null;
        }
        return super.readDefaultValue(columnMetadataResultSet, columnInfo, database);
    }

    //    /**
//     * Derby seems to have troubles
//     */
//    @Override
//    public boolean hasIndex(Schema schema, String tableName, String indexName, String columnNames, Database database) throws DatabaseException {
//        try {
//            ResultSet rs = getMetaData(database).getIndexInfo(schema.getCatalogName(), schema.getName(), "%", false, true);
//            while (rs.next()) {
//                if (database.objectNamesEqual(rs.getString("INDEX_NAME"), indexName)) {
//                    return true;
//                }
//                if (tableName != null && columnNames != null) {
//                    if (database.objectNamesEqual(tableName, rs.getString("TABLE_NAME")) && database.objectNamesEqual(columnNames.replaceAll(" ",""), rs.getString("COLUMN_NAME").replaceAll(" ",""))) {
//                        return true;
//                    }
//                }
//            }
//            return false;
//        } catch (SQLException e) {
//            throw new DatabaseException(e);
//        }
//    }

}