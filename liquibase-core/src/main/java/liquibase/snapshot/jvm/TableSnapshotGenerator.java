package liquibase.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TableSnapshotGenerator extends JdbcDatabaseObjectSnapshotGenerator<Table> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }


    public boolean has(Table example, Database database) throws DatabaseException {
        Schema schema = example.getSchema();
//        if (schema == null) {
//            schema = Schema.DEFAULT;
//        }
        try {
            String tableName = example.getName();
            if (database != null) {
                tableName = database.correctObjectName(tableName, Table.class);
            }
            ResultSet rs = getMetaData(database).getTables(database.getJdbcCatalogName(schema), database.getJdbcSchemaName(schema), tableName, new String[]{"TABLE"});
            try {
                return rs.next();
            } finally {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }


    public Table snapshot(Table example, Database database) throws DatabaseException {
        String objectName = example.getName();
        Schema schema = example.getSchema();

        ResultSet rs = null;
        try {
            DatabaseMetaData metaData = getMetaData(database);
            rs = metaData.getTables(database.getJdbcCatalogName(schema), database.getJdbcSchemaName(schema), database.correctObjectName(objectName, Table.class), new String[]{"TABLE"});

            Table table;
            try {
                if (rs.next()) {
                 table = readTable(rs, database);
                } else {
                    return null;
                }
            } finally {
                rs.close();
            }

            return table;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                }
            }
        }

    }

    public Table[] get(DatabaseObject container, Database database) throws DatabaseException {
        if (!(container instanceof Schema)) {
            return null;
        }
        Schema schema = (Schema) container;

        updateListeners("Reading tables for " + database.toString() + " ...");

        List<Table> returnTables = new ArrayList<Table>();
        ResultSet tableMetaDataRs = null;
        try {
            tableMetaDataRs = getMetaData(database).getTables(database.getJdbcCatalogName(schema), database.getJdbcSchemaName(schema), null, new String[]{"TABLE"});
            while (tableMetaDataRs.next()) {
                Table table = readTable(tableMetaDataRs, database);
                returnTables.add(table);
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            try {
                if (tableMetaDataRs != null) {
                    tableMetaDataRs.close();
                }
            } catch (SQLException ignore) {
            }
        }
        return returnTables.toArray(new Table[returnTables.size()]);
    }

    protected Table readTable(ResultSet tableMetadataResultSet, Database database) throws SQLException, DatabaseException {
        String rawTableName = tableMetadataResultSet.getString("TABLE_NAME");
        String rawSchemaName = StringUtils.trimToNull(tableMetadataResultSet.getString("TABLE_SCHEM"));
        String rawCatalogName = StringUtils.trimToNull(tableMetadataResultSet.getString("TABLE_CAT"));
        String remarks = StringUtils.trimToNull(tableMetadataResultSet.getString("REMARKS"));

        Table table = new Table().setName(cleanNameFromDatabase(rawTableName, database));
        table.setRemarks(remarks);
        table.setDatabase(database);
        table.setRawSchemaName(rawSchemaName);
        table.setRawCatalogName(rawCatalogName);

        CatalogAndSchema schemaFromJdbcInfo = database.getSchemaFromJdbcInfo(rawSchemaName, rawCatalogName);
        table.setSchema(new Schema(schemaFromJdbcInfo.getCatalogName(), schemaFromJdbcInfo.getSchemaName()));

        CatalogAndSchema rawSchema = database.correctSchema(new CatalogAndSchema(table.getRawCatalogName(), table.getRawSchemaName()));
        ResultSet columnMetadataResultSet = getMetaData(database).getColumns(database.getJdbcCatalogName(rawSchema), database.getJdbcSchemaName(rawSchema), rawTableName, null);
        try {
            while (columnMetadataResultSet.next()) {
                table.getColumns().add(new Column().setName(columnMetadataResultSet.getString("COLUMN_NAME")));
            }
        } finally {
            columnMetadataResultSet.close();
        }

        table.setPartial(false);
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
