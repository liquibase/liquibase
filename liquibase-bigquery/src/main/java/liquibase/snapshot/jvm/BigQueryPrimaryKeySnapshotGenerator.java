package liquibase.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.database.BigQueryDatabase;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

import java.util.List;
import java.util.Map;

public class BigQueryPrimaryKeySnapshotGenerator extends PrimaryKeySnapshotGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (!(database instanceof BigQueryDatabase)) {
            return PRIORITY_NONE;
        }
        int priority = super.getPriority(objectType, database);
        if (priority > PRIORITY_NONE) {
            priority += PRIORITY_DATABASE;
        }
        return priority;
    }

    @Override
    public Class<? extends SnapshotGenerator>[] replaces() {
        return new Class[]{PrimaryKeySnapshotGenerator.class};
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        Database database = snapshot.getDatabase();
        Schema schema = example.getSchema();
        String searchTableName = null;
        if (((PrimaryKey) example).getTable() != null) {
            searchTableName = ((PrimaryKey) example).getTable().getName();
            searchTableName = database.correctObjectName(searchTableName, Table.class);
        }
        PrimaryKey returnKey = null;

        String keyColumnUsageStatement = String.format("SELECT * FROM %s.INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE table_name = ?", schema.getSchema());
        Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
        List<Map<String, ?>> maps = executor.queryForList(new RawParameterizedSqlStatement(keyColumnUsageStatement, searchTableName));
        String columnName;
        for (Map<String, ?> map : maps) {
            columnName = map.get("COLUMN_NAME").toString();
            int position = ((Long) map.get("ORDINAL_POSITION")).intValue();

            if (returnKey == null) {
                returnKey = new PrimaryKey();
                String catalogName = (String) map.get("TABLE_CATALOG");
                String schemaName = (String) map.get("TABLE_SCHEMA");
                CatalogAndSchema tableSchema = new CatalogAndSchema(catalogName, schemaName);
                returnKey.setTable((Table) new Table().setName(map.get("TABLE_NAME").toString()).setSchema(new Schema(tableSchema.getCatalogName(), tableSchema.getSchemaName())));
                returnKey.setName(map.get("CONSTRAINT_NAME").toString());
            }

            returnKey.addColumn(position - 1, new Column(columnName)
                    .setRelation(((PrimaryKey) example).getTable()));
        }
        if (returnKey != null) {
            Index exampleIndex = new Index().setRelation(returnKey.getTable());
            exampleIndex.setColumns(returnKey.getColumns());
            returnKey.setBackingIndex(exampleIndex);
        }
        return returnKey;
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException {
        if (!snapshot.getSnapshotControl().shouldInclude(PrimaryKey.class)) {
            return;
        }

        if (foundObject instanceof Table) {
            Table table = (Table) foundObject;
            Database database = snapshot.getDatabase();
            Schema schema = table.getSchema();

            Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
            String tableConstraintsStatement = String.format("SELECT * FROM %s.INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE table_name = ?", schema.getSchema());
            List<Map<String, ?>> maps = executor.queryForList(new RawParameterizedSqlStatement(tableConstraintsStatement, table.getName()));

            for (Map<String, ?> map : maps) {
                if (map.containsKey("CONSTRAINT_NAME")) {
                    String constraintName = map.get("CONSTRAINT_NAME").toString();
                    PrimaryKey primaryKey = new PrimaryKey().setName(constraintName);
                    primaryKey.setTable((Table) foundObject);
                    if (!database.isSystemObject(primaryKey)) {
                        table.setPrimaryKey(primaryKey.setTable(table));
                    }
                }
            }
        }
    }
}
