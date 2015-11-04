package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.QueryJdbcMetaDataAction;
import liquibase.action.core.SnapshotDatabaseObjectsAction;
import liquibase.actionlogic.RowBasedQueryResult;
import liquibase.exception.ActionPerformException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;
import liquibase.util.Validate;

import java.util.List;

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
    protected Action createSnapshotAction(SnapshotDatabaseObjectsAction action, Scope scope) throws ActionPerformException {

        DatabaseObject relatedTo = action.relatedTo;
        ObjectName objectName;
        if (relatedTo instanceof Catalog) {
            if (scope.getDatabase().getMaxSnapshotContainerDepth() < 2) {
                throw new ActionPerformException("Cannot snapshot catalogs on " + scope.getDatabase().getShortName());
            }
            objectName = new ObjectName(relatedTo.getSimpleName(), null, null);
        } else if (relatedTo instanceof Schema) {
            objectName = new ObjectName(new ObjectName(relatedTo.getName().container, relatedTo.getSimpleName()), null);
        } else if (relatedTo instanceof Table) {
            objectName = relatedTo.getName();
        } else {
            throw Validate.failure("Unexpected relatedTo type: " + relatedTo.getClass().getName());
        }

        objectName = objectName.truncate(scope.getDatabase().getMaxSnapshotContainerDepth() + 1);
        List<String> nameParts = objectName.asList(3);

        if (scope.getDatabase().getMaxSnapshotContainerDepth() >= 2) {
            return new QueryJdbcMetaDataAction("getTables", nameParts.get(0), nameParts.get(1), nameParts.get(2), new String[]{"TABLE"});
        } else { //usually calls the top level "catalogs"
            return new QueryJdbcMetaDataAction("getTables", nameParts.get(1), null, nameParts.get(2), new String[]{"TABLE"});
        }
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
        int maxContainerDepth = scope.getDatabase().getMaxReferenceContainerDepth();
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
