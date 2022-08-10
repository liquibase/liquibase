package liquibase.ext.bigquery.snapshot.jvm;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.ext.bigquery.database.BigqueryDatabase;

import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.snapshot.jvm.ColumnSnapshotGenerator;
;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BigQueryColumnSnapshotGenerator extends ColumnSnapshotGenerator {


    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        int priority = super.getPriority(objectType, database);
        if (database instanceof BigqueryDatabase) {
            priority += PRIORITY_DATABASE;
        }
        return priority;
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException {
        System.out.println("Wlazlem ssssssss");
        if (!snapshot.getSnapshotControl().shouldInclude(Column.class)) {
            return;
        }
        if (foundObject instanceof Relation) {
            Database database = snapshot.getDatabase();
            Relation relation = (Relation) foundObject;
            String query = String.format("SELECT KEYSPACE_NAME, COLUMN_NAME, TYPE, KIND FROM system_schema.columns WHERE KEYSPACE_NAME = '%s' AND table_name='%s';"
                    , database.getDefaultCatalogName(), relation.getName());
            Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc",
                    database);
            List<Map<String, ?>> returnList = executor.queryForList(new RawSqlStatement(query));

            for (Map<String, ?> columnPropertiesMap : returnList) {
               // relation.getColumns().add(readColumn(columnPropertiesMap, relation));
            }
        }
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException {
        Database database = snapshot.getDatabase();
        Relation relation = ((Column) example).getRelation();
        //we can't add column name as query parameter here as AWS keyspaces don't support such where statement
        String query = String.format("SELECT KEYSPACE_NAME, COLUMN_NAME, TYPE, KIND FROM system_schema.columns WHERE keyspace_name = '%s' AND table_name='%s';"
                , database.getDefaultCatalogName(), relation);

        List<Map<String, ?>> returnList = Scope.getCurrentScope().getSingleton(ExecutorService.class)
                .getExecutor("jdbc", database).queryForList(new RawSqlStatement(query));
        returnList = returnList.stream()
                .filter(stringMap -> ((String)stringMap.get("COLUMN_NAME")).equalsIgnoreCase(example.getName()))
                .collect(Collectors.toList());
        if (returnList.size() != 1) {
            Scope.getCurrentScope().getLog(BigQueryColumnSnapshotGenerator.class).warning(String.format(
                    "expecting exactly 1 column with name %s, got %s", example.getName(), returnList.size()));
            return null;
        } else {
            return null;//readColumn(returnList.get(0), relation);
        }
    }

}
