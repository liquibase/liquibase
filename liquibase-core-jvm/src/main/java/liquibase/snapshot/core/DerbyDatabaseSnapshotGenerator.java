package liquibase.snapshot.core;

import liquibase.database.Database;
import liquibase.database.structure.Column;
import liquibase.database.structure.ForeignKey;
import liquibase.database.structure.Table;
import liquibase.database.core.DB2Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.RawSqlStatement;

import java.util.List;
import java.util.Map;
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
    public boolean hasIndex(String schemaName, String indexName, Database database) throws DatabaseException {
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