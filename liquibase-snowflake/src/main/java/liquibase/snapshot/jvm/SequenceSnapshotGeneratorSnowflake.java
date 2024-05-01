package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;

public class SequenceSnapshotGeneratorSnowflake extends SequenceSnapshotGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (database instanceof SnowflakeDatabase) {
            return super.getPriority(objectType, database) + PRIORITY_DATABASE;
        } else {
            return PRIORITY_NONE;
        }
    }

    @Override
    public Class<? extends SnapshotGenerator>[] replaces() {
        return new Class[]{SequenceSnapshotGenerator.class};
    }

    @Override
    protected SqlStatement getSelectSequenceStatement(Schema schema, Database database) {
        if (database instanceof SnowflakeDatabase) {
            return new RawSqlStatement("SELECT SEQUENCE_NAME, START_VALUE, MINIMUM_VALUE AS MIN_VALUE, MAXIMUM_VALUE AS MAX_VALUE, " +
                    database.escapeObjectName("INCREMENT", Column.class) + " AS INCREMENT_BY, " +
                    "CYCLE_OPTION AS WILL_CYCLE FROM information_schema.sequences " +
                    "WHERE SEQUENCE_CATALOG='" + database.getDefaultCatalogName() + "' AND " +
                    "SEQUENCE_SCHEMA='" + database.getDefaultSchemaName() + "'");
        }
        return super.getSelectSequenceStatement(schema, database);
    }
}
