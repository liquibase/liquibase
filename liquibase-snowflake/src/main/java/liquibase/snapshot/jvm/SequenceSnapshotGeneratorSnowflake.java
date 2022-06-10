package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;

public class SequenceSnapshotGeneratorSnowflake extends SequenceSnapshotGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        int priority = super.getPriority(objectType, database);
        if (database instanceof SnowflakeDatabase) {
            priority += PRIORITY_DATABASE;
        }
        return priority;
    }

    @Override
    public Class<? extends SnapshotGenerator>[] replaces() {
        return new Class[]{SequenceSnapshotGenerator.class};
    }

    @Override
    protected String getSelectSequenceSql(Schema schema, Database database) {
        if (database instanceof SnowflakeDatabase) {
            return "SELECT SEQUENCE_NAME, START_VALUE, MINIMUM_VALUE AS MIN_VALUE, MAXIMUM_VALUE AS MAX_VALUE, " +
                    database.escapeObjectName("INCREMENT", Column.class) + " AS INCREMENT_BY, " +
                    "CYCLE_OPTION AS WILL_CYCLE FROM information_schema.sequences " +
                    "WHERE SEQUENCE_CATALOG='" + database.getDefaultCatalogName() + "' AND " +
                    "SEQUENCE_SCHEMA='" + database.getDefaultSchemaName() + "'";
        }
        return super.getSelectSequenceSql(schema, database);
    }
}