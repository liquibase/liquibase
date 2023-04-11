package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;
import liquibase.util.JdbcUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SchemaSnapshotGeneratorSnowflake extends SchemaSnapshotGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (database instanceof SnowflakeDatabase) {
            return super.getPriority(objectType, database) + PRIORITY_DATABASE;
        } else {
            return PRIORITY_NONE;
        }
    }

    @Override
    protected String[] getDatabaseSchemaNames(Database database) throws SQLException, DatabaseException {
        List<String> returnList = new ArrayList<>();

        try (ResultSet schemas = ((JdbcConnection) database.getConnection()).getMetaData().getSchemas(database
                .getDefaultCatalogName(), null)) {
            while (schemas.next()) {
                returnList.add(JdbcUtil.getValueForColumn(schemas, "TABLE_SCHEM", database));
            }
        }

        return returnList.toArray(new String[0]);
    }
}
