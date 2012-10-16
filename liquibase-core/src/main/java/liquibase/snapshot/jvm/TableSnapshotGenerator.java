package liquibase.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.snapshot.SnapshotGeneratorChain;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.StringUtils;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TableSnapshotGenerator extends JdbcSnapshotGenerator {
    public TableSnapshotGenerator() {
        super(Table.class, new Class[] {Schema.class});
    }

//    public Boolean has(DatabaseObject example, DatabaseSnapshot snapshot, SnapshotGeneratorChain chain) throws DatabaseException {
//        if (!(example instanceof Table)) {
//            return chain.has(example, snapshot);
//        }
//        Database database = snapshot.getDatabase();
//        Schema schema = example.getSchema();
////        if (schema == null) {
////            schema = Schema.DEFAULT;
////        }
//        try {
//            String tableName = example.getName();
//            if (snapshot != null) {
//                tableName = database.correctObjectName(tableName, Table.class);
//            }
//            ResultSet rs = getMetaData(database).getTables(database.getJdbcCatalogName(schema), database.getJdbcSchemaName(schema), tableName, new String[]{"TABLE"});
//            try {
//                return rs.next();
//            } finally {
//                try {
//                    rs.close();
//                } catch (SQLException ignore) {
//                }
//            }
//        } catch (SQLException e) {
//            throw new DatabaseException(e);
//        }
//    }


    public DatabaseObject snapshot(DatabaseObject example, DatabaseSnapshot snapshot, SnapshotGeneratorChain chain) throws DatabaseException, InvalidExampleException {
        Database database = snapshot.getDatabase();
        if (example instanceof Table) {
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
        } else if (example instanceof Schema) {
            Schema schema = (Schema) chain.snapshot(example, snapshot);

            if (schema != null) {
                ResultSet tableMetaDataRs = null;
                try {
                    tableMetaDataRs = getMetaData(database).getTables(database.getJdbcCatalogName(schema), database.getJdbcSchemaName(schema), null, new String[]{"TABLE"});
                    while (tableMetaDataRs.next()) {
                        String tableName = tableMetaDataRs.getString("TABLE_NAME");
                        Table tableExample = (Table) new Table().setName(tableName).setSchema(schema);

                        schema.addDatabaseObject(snapshot.snapshot(tableExample));
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
            }
            return schema;

        }  else {
            throw new UnexpectedLiquibaseException("Unexpected snapshot example: "+example.getClass().getName());
        }

    }

    protected Table readTable(ResultSet tableMetadataResultSet, Database database) throws SQLException, DatabaseException {
        String rawTableName = tableMetadataResultSet.getString("TABLE_NAME");
        String rawSchemaName = StringUtils.trimToNull(tableMetadataResultSet.getString("TABLE_SCHEM"));
        String rawCatalogName = StringUtils.trimToNull(tableMetadataResultSet.getString("TABLE_CAT"));
        String remarks = StringUtils.trimToNull(tableMetadataResultSet.getString("REMARKS"));

        Table table = new Table().setName(cleanNameFromDatabase(rawTableName, database));
        table.setRemarks(remarks);

        CatalogAndSchema schemaFromJdbcInfo = database.getSchemaFromJdbcInfo(rawCatalogName, rawSchemaName);
        table.setSchema(new Schema(schemaFromJdbcInfo.getCatalogName(), schemaFromJdbcInfo.getSchemaName()));

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
