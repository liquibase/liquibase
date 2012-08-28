package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.structure.Schema;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.RawSqlStatement;

import java.util.List;
import java.util.Map;

public class DB2DatabaseSnapshotGenerator extends JdbcDatabaseSnapshotGenerator {
    public boolean supports(Database database) {
        return database instanceof DB2Database;
    }

    public int getPriority(Database database) {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean isColumnAutoIncrement(Database database, Schema schema, String tableName, String columnName) throws DatabaseException {
        schema = database.correctSchema(schema);

        boolean autoIncrement = false;

        List<Map> rs = ExecutorService.getInstance().getExecutor(database).queryForList(new RawSqlStatement("SELECT IDENTITY FROM SYSCAT.COLUMNS WHERE TABSCHEMA = '" + schema.getName() + "' AND TABNAME = '" + tableName + "' AND COLNAME = '" + columnName + "' AND HIDDEN != 'S'"));

        for (Map row : rs) {
            String identity = row.get("IDENTITY").toString();
            if (identity.equalsIgnoreCase("Y")) {
                autoIncrement = true;
            }
        }

        return autoIncrement;
    }

    @Override
    protected String getJdbcCatalogName(Schema schema) {
        return null;
    }

    @Override
    protected String getJdbcSchemaName(Schema schema) {
        return schema.getCatalogName();
    }

    @Override
    protected Schema getSchemaFromJdbcInfo(String rawSchemaName, String rawCatalogName, Database database) {
        return database.correctSchema(new Schema(rawSchemaName, null));
    }

}
