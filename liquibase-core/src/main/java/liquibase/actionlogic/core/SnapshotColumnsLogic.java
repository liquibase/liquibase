package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.QueryJdbcMetaDataAction;
import liquibase.action.core.SnapshotDatabaseObjectsAction;
import liquibase.actionlogic.RowBasedQueryResult;
import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.Validate;

/**
 * Logic to snapshot database column(s). Delegates to {@link QueryJdbcMetaDataAction} getColumns().
 */
public class SnapshotColumnsLogic extends AbstractSnapshotDatabaseObjectsLogic {

    @Override
    protected Class<? extends DatabaseObject> getTypeToSnapshot() {
        return Column.class;
    }

    @Override
    protected Class<? extends DatabaseObject>[] getSupportedRelatedTypes() {
        return new Class[] {
                Column.class,
                Relation.class,
                Schema.class,
                Catalog.class
        };
    }

    @Override
    protected Action createSnapshotAction(Action action, Scope scope) throws DatabaseException {
        DatabaseObject relatedTo = action.get(SnapshotDatabaseObjectsAction.Attr.relatedTo, DatabaseObject.class);

        String catalogName = null;
        String schemaName = null;
        String relationName = null;
        String columnName = null;

        if (relatedTo instanceof Catalog) {
            catalogName = relatedTo.getName();
        } else if (relatedTo instanceof Schema) {
            catalogName = ((Schema) relatedTo).getCatalogName();
            schemaName = relatedTo.getName();
        } else if (relatedTo instanceof Relation) {
            relationName = relatedTo.getName();

            Schema schema = relatedTo.getSchema();
            if (schema != null) {
                catalogName = schema.getCatalogName();
                schemaName = schema.getName();
            }
        } else if (relatedTo instanceof Column) {
            columnName = relatedTo.getName();

            Relation relation = ((Column) relatedTo).getRelation();
            relationName = relation.getName();

            Schema schema = relation.getSchema();
            if (schema != null) {
                catalogName = schema.getCatalogName();
                schemaName = schema.getName();
            }
        } else {
            throw Validate.failure("Unexpected type: "+relatedTo.getClass().getName());
        }

        return new QueryJdbcMetaDataAction("getColumns", catalogName, schemaName, relationName, columnName);

    }

    @Override
    protected DatabaseObject convertToObject(RowBasedQueryResult.Row row, Action originalAction, Scope scope) {
        return null; //TODO
    }
}
