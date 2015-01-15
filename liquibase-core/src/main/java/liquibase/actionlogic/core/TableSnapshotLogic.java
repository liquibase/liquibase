package liquibase.actionlogic.core;

import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.util.JdbcUtils;
import liquibase.util.SmartMap;
import liquibase.util.StringUtils;
import liquibase.util.Validate;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class TableSnapshotLogic extends AbstractJdbcMetaDataLogic {

    @Override
    protected Class<? extends DatabaseObject> getTypeToSnapshot() {
        return Table.class;
    }

    @Override
    protected Class<? extends DatabaseObject>[] getSupportedBaseObject() {
        return new Class[]{
                Schema.class,
                Catalog.class,
                Table.class
        };
    }

    @Override
    protected List<SmartMap> readRawMetaData(DatabaseObject relatedTo, Class<? extends DatabaseObject> typeToSnapshot, DatabaseMetaData metaData, Scope scope) throws SQLException {
        String catalogName = null;
        String schemaName = null;
        String tableName = null;

        if (Catalog.class.isAssignableFrom(relatedTo.getClass())) {
            catalogName = relatedTo.getName();
        } else if (Schema.class.isAssignableFrom(relatedTo.getClass())) {
            catalogName = ((Schema) relatedTo).getCatalogName();
            schemaName = relatedTo.getName();
        } else if (Table.class.isAssignableFrom(relatedTo.getClass())) {
            Table table = (Table) relatedTo;
            if (table.getSchema() != null) {
                catalogName = table.getSchema().getCatalogName();
                schemaName = table.getSchema().getName();
            }
            tableName = table.getName();
        } else {
            throw Validate.failure("Unexpected relatedTo type: " + relatedTo.getClass());
        }

        return JdbcUtils.extract(metaData.getTables(catalogName, schemaName, tableName, new String[]{"TABLE"}));
    }

    @Override
    protected DatabaseObject convertToObject(SmartMap row, Class outputType, Scope scope) {
        String rawTableName = row.get("TABLE_NAME", String.class);
        String rawSchemaName = row.get("TABLE_SCHEM", String.class);
        String rawCatalogName = row.get("TABLE_CAT", String.class);
        String remarks = StringUtils.trimToNull(row.get("REMARKS", String.class));
        if (remarks != null) {
            remarks = remarks.replace("''", "'"); //come back escaped sometimes
        }

        Table table = new Table().setName(cleanNameFromDatabase(rawTableName, scope));
        table.setRemarks(remarks);

        Database database = scope.get(Scope.Attr.database, Database.class);
        CatalogAndSchema schemaFromJdbcInfo = ((AbstractJdbcDatabase) database).getSchemaFromJdbcInfo(rawCatalogName, rawSchemaName);
        table.setSchema(new Schema(schemaFromJdbcInfo.getCatalogName(), schemaFromJdbcInfo.getSchemaName()));

        if ("Y".equals(row.get("TEMPORARY", String.class))) {
            table.setAttribute("temporary", "GLOBAL");

            String duration = row.get("DURATION", String.class);
            if (duration != null && duration.equals("SYS$TRANSACTION")) {
                table.setAttribute("duration", "ON COMMIT DELETE ROWS");
            } else if (duration != null && duration.equals("SYS$SESSION")) {
                table.setAttribute("duration", "ON COMMIT PRESERVE ROWS");
            }
        }

        return table;
    }
}
