package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.SnapshotDatabaseObjectsAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.ObjectBasedQueryResult;
import liquibase.actionlogic.RowBasedQueryResult;
import liquibase.exception.ActionPerformException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.*;

import java.util.*;

public class SnapshotPrimaryKeysLogicJdbc extends AbstractSnapshotDatabaseObjectsLogicJdbc {

    @Override
    protected Class<? extends DatabaseObject> getTypeToSnapshot() {
        return PrimaryKey.class;
    }

    @Override
    protected Class<? extends DatabaseObject>[] getSupportedRelatedTypes() {
        return new Class[]{
                PrimaryKey.class,
                Relation.class,
                Schema.class,
                Catalog.class
        };
    }

    @Override
    protected Action createSnapshotAction(SnapshotDatabaseObjectsAction action, Scope scope) throws ActionPerformException {
//        DatabaseObject relatedTo = action.relatedTo;
//        ObjectReference objectReference;
//        if (relatedTo instanceof Catalog) {
//            if (scope.getDatabase().getMaxSnapshotContainerDepth() < 2) {
//                throw new ActionPerformException("Cannot snapshot catalogs on " + scope.getDatabase().getShortName());
//            }
//            objectReference = new ObjectReference(relatedTo.getName(), null, null, null);
//        } else if (relatedTo instanceof Schema) {
//            objectReference = new ObjectReference(new ObjectReference(new ObjectReference(relatedTo.getName().container, relatedTo.getName()), null), null);
//        } else if (relatedTo instanceof Table) {
//            objectReference = new ObjectReference(relatedTo.getName(), null);
//        } else if (relatedTo instanceof PrimaryKey) {
//            objectReference = relatedTo.getName();
//        } else {
//            throw Validate.failure("Unexpected relatedTo type: " + relatedTo.getClass().getName());
//        }

//        objectReference = objectReference.truncate(scope.getDatabase().getMaxSnapshotContainerDepth() + 2);
//        List<String> nameParts = objectReference.asList(4);

//        if (scope.getDatabase().getMaxSnapshotContainerDepth() >= 3) {
//            return new QueryJdbcMetaDataAction("getPrimaryKeys", nameParts.get(0), nameParts.get(1), nameParts.get(2));
//        } else { //usually calls the top level "catalogs"
//            return new QueryJdbcMetaDataAction("getPrimaryKeys", nameParts.get(1), null, nameParts.get(2));
//        }
        return null;
    }

    @Override
    protected DatabaseObject convertToObject(RowBasedQueryResult.Row row, SnapshotDatabaseObjectsAction originalAction, Scope scope) throws ActionPerformException {
        String pkName = row.get("PK_NAME", String.class);
        String columnName = row.get("COLUMN_NAME", String.class);
        Integer position = row.get("KEY_SEQ", Integer.class);
        String ascOrDesc = row.get("ASC_OR_DESC", String.class);
        Boolean descending = "D".equals(ascOrDesc) ? Boolean.TRUE : "A".equals(ascOrDesc) ? Boolean.FALSE : null;

        String tableCat = row.get("TABLE_CAT", String.class);
        String tableSchema = row.get("TABLE_SCHEM", String.class);
        String tableName = row.get("TABLE_NAME", String.class);

        ObjectReference objectReference;
        if (tableCat != null && tableSchema == null) {
            objectReference = new ObjectReference(tableCat, tableName, pkName);
        } else {
            objectReference = new ObjectReference(tableCat, tableSchema, tableName, pkName);
        }

        PrimaryKey pk = new PrimaryKey(objectReference);
//        PrimaryKey.PrimaryKeyColumn pkColumn = new PrimaryKey.PrimaryKeyColumn(objectReference.container, columnName);
//        pkColumn.descending = descending;
//        pkColumn.position = position;
//        pk.columns.add(pkColumn);

        return pk;

//                Index exampleIndex = new Index().setTable(returnKey.getTable());
//                exampleIndex.setColumns(returnKey.getColumns());
//todo: move for action logic                if (database instanceof MSSQLDatabase) { //index name matches PK name for better accuracy
//                    exampleIndex.setName(returnKey.getName());
//                }
//                returnKey.setBackingIndex(exampleIndex);
//    }

//    return returnKey;

}

    @Override
    protected ActionResult.Modifier createModifier(SnapshotDatabaseObjectsAction originalAction, Scope scope) {
        return new SnapshotModifier(originalAction, scope) {
            @Override
            public ActionResult rewrite(ActionResult result) throws ActionPerformException {
                List<PrimaryKey> rawResults = ((ObjectBasedQueryResult) super.rewrite(result)).asList(PrimaryKey.class);
                Map<ObjectReference, PrimaryKey> combinedResults = new HashMap<>();
                for (PrimaryKey primaryKey : rawResults) {
                    PrimaryKey existingPk = combinedResults.get(primaryKey.name);
//                    if (existingPk == null) {
//                        combinedResults.put(primaryKey.name, primaryKey);
//                    } else {
//                        existingPk.columns.addAll(primaryKey.columns);
//                    }
                }

                for (PrimaryKey primaryKey : combinedResults.values()) {
                    Collections.sort(primaryKey.columns, new Comparator<PrimaryKey.PrimaryKeyColumn>() {
                        @Override
                        public int compare(PrimaryKey.PrimaryKeyColumn o1, PrimaryKey.PrimaryKeyColumn o2) {
//                            if (o1.position == null || o2.position == null) {
//                                return o1.name.compareTo(o2.name);
//                            } else {
//                                return o1.position.compareTo(o2.position);
//                            }
                            return 0;
                        }
                    });
                }

                return new ObjectBasedQueryResult(new ArrayList(combinedResults.values()));
            }
        };
    }
}
