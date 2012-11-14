package liquibase.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.statement.core.GetViewDefinitionStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.View;
import liquibase.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ViewSnapshotGenerator extends JdbcSnapshotGenerator {

    public ViewSnapshotGenerator() {
        super(View.class, new Class[] { Schema.class });
    }

//    public Boolean has(DatabaseObject example, DatabaseSnapshot snapshot, SnapshotGeneratorChain chain) throws DatabaseException {
//        Database database = snapshot.getDatabase();
//        if (!(example instanceof View)) {
//            return chain.has(example, snapshot);
//        }
//        String viewName = example.getName();
//        Schema schema = example.getSchema();
//        try {
//            ResultSet rs = getMetaData(database).getTables(database.getJdbcCatalogName(schema), database.getJdbcSchemaName(schema), database.correctObjectName(viewName, View.class), new String[]{"VIEW"});
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


    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException {
        Database database = snapshot.getDatabase();
        Schema schema = example.getSchema();

        ResultSet viewsMetadataRs = null;
        try {
            viewsMetadataRs = getMetaData(database).getTables(((AbstractJdbcDatabase) database).getJdbcCatalogName(schema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(schema), example.getName(), new String[]{"VIEW"});
            if (viewsMetadataRs.next()) {
                String rawViewName = viewsMetadataRs.getString("TABLE_NAME");
                String rawSchemaName = StringUtils.trimToNull(viewsMetadataRs.getString("TABLE_SCHEM"));
                String rawCatalogName = StringUtils.trimToNull(viewsMetadataRs.getString("TABLE_CAT"));
                String remarks = viewsMetadataRs.getString("REMARKS");

                View view = new View().setName(cleanNameFromDatabase(rawViewName, database));
                view.setRemarks(remarks);

                CatalogAndSchema schemaFromJdbcInfo = ((AbstractJdbcDatabase) database).getSchemaFromJdbcInfo(rawCatalogName, rawSchemaName);
                view.setSchema(new Schema(schemaFromJdbcInfo.getCatalogName(), schemaFromJdbcInfo.getSchemaName()));

                try {
                    view.setDefinition(database.getViewDefinition(schemaFromJdbcInfo, view.getName()));
                } catch (DatabaseException e) {
                    throw new DatabaseException("Error getting " + database.getConnection().getURL() + " view with " + new GetViewDefinitionStatement(view.getSchema().getCatalog().getName(), view.getSchema().getName(), rawViewName), e);
                }

                return view;
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            try {
                if (viewsMetadataRs != null) {
                    viewsMetadataRs.close();
                }
            } catch (SQLException ignore) {
            }
        }
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (foundObject instanceof Schema) {
            Schema schema = (Schema) foundObject;
            Database database = snapshot.getDatabase();
            ResultSet viewsMetadataRs = null;
            try {
                viewsMetadataRs = getMetaData(database).getTables(((AbstractJdbcDatabase) database).getJdbcCatalogName(schema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(schema), null, new String[]{"VIEW"});
                while (viewsMetadataRs.next()) {
                    schema.addDatabaseObject(new View().setName(viewsMetadataRs.getString("TABLE_NAME")).setSchema(schema));
                }
            } catch (SQLException e) {
                throw new DatabaseException(e);
            } finally {
                try {
                    if (viewsMetadataRs != null) {
                        viewsMetadataRs.close();
                    }
                } catch (SQLException ignore) {
                }
            }
        }
    }

    //from SQLIteSnapshotGenerator
    //    protected void readViews(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws SQLException, DatabaseException {
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
