package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.QueryJdbcMetaDataAction;
import liquibase.action.core.SnapshotDatabaseObjectsAction;
import liquibase.actionlogic.RowBasedQueryResult;
import liquibase.exception.ActionPerformException;
import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectName;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;
import liquibase.util.Validate;

/**
 * Logic to snapshot database table(s). Delegates to {@link QueryJdbcMetaDataAction} getTables().
 */
public class SnapshotTablesLogicJdbc extends AbstractSnapshotDatabaseObjectsLogicJdbc {

    @Override
    protected Class<? extends DatabaseObject> getTypeToSnapshot() {
        return Table.class;
    }

    @Override
    protected Class<? extends DatabaseObject>[] getSupportedRelatedTypes() {
        return new Class[]{
                Schema.class,
                Catalog.class,
                Table.class
        };
    }

    @Override
    protected Action createSnapshotAction(SnapshotDatabaseObjectsAction action, Scope scope) throws DatabaseException {

        ObjectReference relatedTo = action.relatedTo;
        String catalogName = null;
        String schemaName = null;
        String tableName = null;

        if (relatedTo.instanceOf(Catalog.class)) {
            catalogName = relatedTo.getSimpleName();
        } else if (Schema.class.isAssignableFrom(relatedTo.getClass())) {
            if (relatedTo.objectName.container != null && scope.getDatabase().getMaxContainerDepth(Table.class) > 1) {
                catalogName = relatedTo.objectName.container.name;
            }
            schemaName = relatedTo.getSimpleName();
        } else if (relatedTo.instanceOf(Table.class)) {
            ObjectName name = relatedTo.objectName;
            tableName = relatedTo.getSimpleName();
            if (name != null) {
                if (name.container != null) {
                    schemaName = name.container.name;
                    if (name.container.container != null) {
                        catalogName = name.container.container.name;
                    }
                }
            }
        } else {
            throw Validate.failure("Unexpected relatedTo type: " + relatedTo.getClass());
        }

        return new QueryJdbcMetaDataAction("getTables", catalogName, schemaName, tableName, new String[]{"TABLE"});
    }

    @Override
    protected DatabaseObject convertToObject(RowBasedQueryResult.Row row, SnapshotDatabaseObjectsAction originalAction, Scope scope) throws ActionPerformException {
        String rawTableName = row.get("TABLE_NAME", String.class);
        String rawSchemaName = row.get("TABLE_SCHEM", String.class);
        String rawCatalogName = row.get("TABLE_CAT", String.class);
        String remarks = StringUtils.trimToNull(row.get("REMARKS", String.class));
        if (remarks != null) {
            remarks = remarks.replace("''", "'"); //come back escaped sometimes
        }

        ObjectName container;
        int maxContainerDepth = scope.getDatabase().getMaxContainerDepth(Table.class);
        if (maxContainerDepth == 0) {
            container = null;
        } else if (maxContainerDepth == 1) {
            if (rawCatalogName != null && rawSchemaName == null) {
                container = new ObjectName(rawCatalogName);
            } else {
                container = new ObjectName(rawSchemaName);
            }
        } else {
            container = new ObjectName(rawCatalogName, rawSchemaName);
        }

        Table table = new Table().setName(new ObjectName(container, rawTableName));
        table.remarks = remarks;

        if ("Y".equals(row.get("TEMPORARY", String.class))) {
            table.set("temporary", "GLOBAL");

            String duration = row.get("DURATION", String.class);
            if (duration != null && duration.equals("SYS$TRANSACTION")) {
                table.set("duration", "ON COMMIT DELETE ROWS");
            } else if (duration != null && duration.equals("SYS$SESSION")) {
                table.set("duration", "ON COMMIT PRESERVE ROWS");
            }
        }

        return table;
    }
}
