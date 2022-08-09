package liquibase.ext.bigquery.snapshot;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.ext.bigquery.database.BigqueryDatabase;
import liquibase.snapshot.*;
import liquibase.snapshot.jvm.TableSnapshotGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.util.BooleanUtil;
import liquibase.util.StringUtil;

import java.sql.SQLException;

public class BigQueryTableSnapshotGenerator extends TableSnapshotGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        int priority = super.getPriority(objectType, database);
        if (database instanceof BigqueryDatabase) {
            priority += PRIORITY_DATABASE;
        }
        return priority;
    }

    @Override
    public Class<? extends SnapshotGenerator>[] replaces() {
        return new Class[] { BigQueryTableSnapshotGenerator.class };
    }

    @Override
    protected Table readTable(CachedRow tableMetadataResultSet, Database database) throws SQLException, DatabaseException {
        String rawTableName = tableMetadataResultSet.getString("TABLE_NAME");
        String rawSchemaName = StringUtil.trimToNull(tableMetadataResultSet.getString("TABLE_SCHEM"));
        String rawCatalogName = StringUtil.trimToNull(tableMetadataResultSet.getString("TABLE_CAT"));
        String remarks = StringUtil.trimToNull(tableMetadataResultSet.getString("REMARKS"));
        String tablespace = StringUtil.trimToNull(tableMetadataResultSet.getString("TABLESPACE_NAME"));
        String defaultTablespaceString = StringUtil.trimToNull(tableMetadataResultSet.getString("DEFAULT_TABLESPACE"));
        if (remarks != null) {
            remarks = remarks.replace("''", "'");
        }

        Table table = (new Table()).setName(this.cleanNameFromDatabase(rawTableName, database));
        table.setRemarks(remarks);
        table.setTablespace(tablespace);
        table.setDefaultTablespace(BooleanUtil.isTrue(Boolean.parseBoolean(defaultTablespaceString)));
        CatalogAndSchema schemaFromJdbcInfo = ((AbstractJdbcDatabase)database).getSchemaFromJdbcInfo(rawCatalogName, rawSchemaName);
        table.setSchema(new Schema(schemaFromJdbcInfo.getCatalogName(), schemaFromJdbcInfo.getSchemaName()));
        if ("Y".equals(tableMetadataResultSet.getString("TEMPORARY"))) {
            table.setAttribute("temporary", "GLOBAL");
            String duration = tableMetadataResultSet.getString("DURATION");
            if (duration != null && "SYS$TRANSACTION".equals(duration)) {
                table.setAttribute("duration", "ON COMMIT DELETE ROWS");
            } else if (duration != null && "SYS$SESSION".equals(duration)) {
                table.setAttribute("duration", "ON COMMIT PRESERVE ROWS");
            }
        }

        return table;
    }



}

