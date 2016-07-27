package liquibase.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.CachedRow;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.JdbcDatabaseSnapshot;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.StringUtils;

import java.sql.SQLException;
import java.util.List;

public class TableSnapshotGenerator extends JdbcSnapshotGenerator {
    public TableSnapshotGenerator() {
        super(Table.class, new Class[] { Schema.class});
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException {
        Database database = snapshot.getDatabase();
        String objectName = example.getName();
        Schema schema = example.getSchema();

        List<CachedRow> rs = null;
        try {
            JdbcDatabaseSnapshot.CachingDatabaseMetaData metaData = ((JdbcDatabaseSnapshot) snapshot).getMetaData();
            rs = metaData.getTables(((AbstractJdbcDatabase) database).getJdbcCatalogName(schema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(schema), objectName);

            Table table;
            if (rs.size() > 0) {
                table = readTable(rs.get(0), database);
            } else {
                return null;
            }

            if (table != null && database instanceof MSSQLDatabase) {
                String schemaName;
                Schema tableSchema = table.getSchema();
                if (tableSchema == null) {
                    schemaName = database.getDefaultSchemaName();
                } else {
                    schemaName = tableSchema.getName();
                }

                String sql;
                if (database.getDatabaseMajorVersion() >=9 ) {
                    sql = "SELECT" +
                            " CAST(value as varchar(max)) as REMARKS" +
                            " FROM" +
                            " sys.extended_properties" +
                            " WHERE" +
                            " name='MS_Description'" +
                            " AND major_id = OBJECT_ID('" + database.escapeStringForDatabase(database.escapeTableName(null, schemaName, table.getName())) + "')" +
                            " AND" +
                            " minor_id = 0";
                } else {
                    sql = "SELECT CAST(value as varchar) as REMARKS FROM dbo.sysproperties WHERE name='MS_Description' AND id = OBJECT_ID('" + database.escapeStringForDatabase(database.escapeTableName(null, schemaName, table.getName())) + "') AND smallid = 0";
                }

                List<String> remarks = ExecutorService.getInstance().getExecutor(snapshot.getDatabase()).queryForList(new RawSqlStatement(sql), String.class);

                if (remarks != null && remarks.size() > 0) {
                    table.setRemarks(StringUtils.trimToNull(remarks.iterator().next()));
                }
            }


            return table;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!snapshot.getSnapshotControl().shouldInclude(Table.class)) {
            return;
        }

        if (foundObject instanceof Schema) {

            Database database = snapshot.getDatabase();
            Schema schema = (Schema) foundObject;

            List<CachedRow> tableMetaDataRs = null;
            try {
                tableMetaDataRs = ((JdbcDatabaseSnapshot) snapshot).getMetaData().getTables(((AbstractJdbcDatabase) database).getJdbcCatalogName(schema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(schema), null);
                for (CachedRow row : tableMetaDataRs) {
                    String tableName = row.getString("TABLE_NAME");
                    Table tableExample = (Table) new Table().setName(cleanNameFromDatabase(tableName, database)).setSchema(schema);

                    schema.addDatabaseObject(tableExample);
                }
            } catch (SQLException e) {
                throw new DatabaseException(e);
            }
        }


    }

    protected Table readTable(CachedRow tableMetadataResultSet, Database database) throws SQLException, DatabaseException {
        String rawTableName = tableMetadataResultSet.getString("TABLE_NAME");
        String rawSchemaName = StringUtils.trimToNull(tableMetadataResultSet.getString("TABLE_SCHEM"));
        String rawCatalogName = StringUtils.trimToNull(tableMetadataResultSet.getString("TABLE_CAT"));
        String remarks = StringUtils.trimToNull(tableMetadataResultSet.getString("REMARKS"));
        if (remarks != null) {
            remarks = remarks.replace("''", "'"); //come back escaped sometimes
        }

        Table table = new Table().setName(cleanNameFromDatabase(rawTableName, database));
        table.setRemarks(remarks);

        CatalogAndSchema schemaFromJdbcInfo = ((AbstractJdbcDatabase) database).getSchemaFromJdbcInfo(rawCatalogName, rawSchemaName);
        table.setSchema(new Schema(schemaFromJdbcInfo.getCatalogName(), schemaFromJdbcInfo.getSchemaName()));

        if ("Y".equals(tableMetadataResultSet.getString("TEMPORARY"))) {
            table.setAttribute("temporary", "GLOBAL");

            String duration = tableMetadataResultSet.getString("DURATION");
            if (duration != null && duration.equals("SYS$TRANSACTION")) {
                table.setAttribute("duration", "ON COMMIT DELETE ROWS");
            } else if (duration != null && duration.equals("SYS$SESSION")) {
                table.setAttribute("duration", "ON COMMIT PRESERVE ROWS");
            }
        }

        return table;
    }

    //code from SqlLiteSnapshotGenerator
    //    protected void readTables(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws SQLException, DatabaseException {
//
//        Database database = snapshot.getDatabase();
//
//        updateListeners("Reading tables for " + database.toString() + " ...");
//        ResultSet rs = databaseMetaData.getTables(
//                database.convertRequestedSchemaToCatalog(schema),
//                database.convertRequestedSchemaToSchema(schema),
//                null,
//                new String[]{"TABLE", "VIEW"});
//
//        try {
//            while (rs.next()) {
//                String type = rs.getString("TABLE_TYPE");
//                String name = rs.getString("TABLE_NAME");
//                String schemaName = rs.getString("TABLE_SCHEM");
//                String catalogName = rs.getString("TABLE_CAT");
//                String remarks = rs.getString("REMARKS");
//
//                if (database.isSystemTable(catalogName, schemaName, name) ||
//                        database.isLiquibaseTable(name) ||
//                        database.isSystemView(catalogName, schemaName, name)) {
//                    continue;
//                }
//
//                if ("TABLE".equals(type)) {
//                    Table table = new Table(name);
//                    table.setRemarks(StringUtils.trimToNull(remarks));
//                    table.setDatabase(database);
//                    table.setSchema(schemaName);
//                    snapshot.getTables().add(table);
//                } else if ("VIEW".equals(type)) {
//                    View view = new View(name);
//                    view.setSchema(schemaName);
//                    try {
//                        view.setDefinition(database.
//                                getViewDefinition(schema, name));
//                    } catch (DatabaseException e) {
//                        System.out.println("Error getting view with " + new GetViewDefinitionStatement(schema, name));
//                        throw e;
//                    }
//                    snapshot.getViews().add(view);
//                }
//            }
//        } finally {
//            rs.close();
//        }
//    }
}
