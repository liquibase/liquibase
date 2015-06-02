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
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;
import liquibase.util.Validate;

/**
 * Logic to snapshot database table(s). Delegates to {@link QueryJdbcMetaDataAction} getTables().
 */
public class SnapshotTablesLogic extends AbstractSnapshotDatabaseObjectsLogic {

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
    protected Action createSnapshotAction(Action action, Scope scope) throws DatabaseException {

        DatabaseObject relatedTo = action.get(SnapshotDatabaseObjectsAction.Attr.relatedTo, DatabaseObject.class);
        String catalogName = null;
        String schemaName = null;
        String tableName = null;

        if (Catalog.class.isAssignableFrom(relatedTo.getClass())) {
            catalogName = relatedTo.getSimpleName();
        } else if (Schema.class.isAssignableFrom(relatedTo.getClass())) {
            if (relatedTo.getName().getContainer() != null && scope.getDatabase().getMaxContainerDepth(Table.class) > 1) {
                catalogName = relatedTo.getName().getContainer().getName();
            }
            schemaName = relatedTo.getSimpleName();
        } else if (Table.class.isAssignableFrom(relatedTo.getClass())) {
            Table table = (Table) relatedTo;
            ObjectName name = table.get(Relation.Attr.name, ObjectName.class);
            if (name != null) {
                tableName = table.getSimpleName();
                if (name.getContainer() != null) {
                    schemaName = name.getContainer().getName();
                    if (name.getContainer().getContainer() != null) {
                        catalogName = name.getContainer().getContainer().getName();
                    }
                }
            }
        } else {
            throw Validate.failure("Unexpected relatedTo type: " + relatedTo.getClass());
        }

        return new QueryJdbcMetaDataAction("getTables", catalogName, schemaName, tableName, new String[]{"TABLE"});
    }

    @Override
    protected DatabaseObject convertToObject(RowBasedQueryResult.Row row, Action originalAction, Scope scope) throws ActionPerformException {
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
        table.setRemarks(remarks);

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
