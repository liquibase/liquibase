package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.snapshot.CachedRow;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.statement.DatabaseFunction;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;

public class ColumnSnapshotGeneratorH2 extends ColumnSnapshotGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Column.class.isAssignableFrom(objectType) && (database instanceof H2Database)) {
            return PRIORITY_DATABASE;
        } else {
            return PRIORITY_NONE;
        }
    }

    @Override
    public Class<? extends SnapshotGenerator>[] replaces() {
        return new Class[]{ColumnSnapshotGenerator.class};
    }


    @Override
    protected Object readDefaultValue(CachedRow columnMetadataResultSet, Column columnInfo, Database database) {
        Object defaultValue = super.readDefaultValue(columnMetadataResultSet, columnInfo, database);
        if ((defaultValue != null) && (defaultValue instanceof DatabaseFunction) && ((DatabaseFunction) defaultValue)
            .getValue().startsWith("NEXT VALUE FOR ")) {
            columnInfo.setAutoIncrementInformation(new Column.AutoIncrementInformation());
            return null;
        }
        return defaultValue;
    }

}
