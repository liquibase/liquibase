package liquibase.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.CachedRow;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.JdbcDatabaseSnapshot;
import liquibase.statement.core.GetViewDefinitionStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.View;
import liquibase.util.StringUtils;

import java.sql.SQLException;
import java.util.List;

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
//            ResultSet rs = getMetaDataFromCache(database).getTables(database.getJdbcCatalogName(schema), database.getJdbcSchemaName(schema), database.correctObjectName(viewName, View.class), new String[]{"VIEW"});
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
        if (((View) example).getDefinition() != null) {
            return example;
        }
        Database database = snapshot.getDatabase();
        Schema schema = example.getSchema();

        List<CachedRow> viewsMetadataRs = null;
        try {
            viewsMetadataRs = ((JdbcDatabaseSnapshot) snapshot).getMetaDataFromCache().getViews(((AbstractJdbcDatabase) database).getJdbcCatalogName(schema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(schema), example.getName());
            if (!viewsMetadataRs.isEmpty()) {
                CachedRow row = viewsMetadataRs.get(0);
                String rawViewName = row.getString("TABLE_NAME");
                String rawSchemaName = StringUtils.trimToNull(row.getString("TABLE_SCHEM"));
                String rawCatalogName = StringUtils.trimToNull(row.getString("TABLE_CAT"));
                String remarks = row.getString("REMARKS");
                if (remarks != null) {
                    remarks = remarks.replace("''", "'"); //come back escaped sometimes
                }

                View view = new View().setName(cleanNameFromDatabase(rawViewName, database));
                view.setRemarks(remarks);
                CatalogAndSchema schemaFromJdbcInfo = ((AbstractJdbcDatabase) database).getSchemaFromJdbcInfo(rawCatalogName, rawSchemaName);
                view.setSchema(new Schema(schemaFromJdbcInfo.getCatalogName(), schemaFromJdbcInfo.getSchemaName()));

                try {
                    String definition = database.getViewDefinition(schemaFromJdbcInfo, view.getName());

                    if (definition.startsWith("FULL_DEFINITION: ")) {
                        definition = definition.replaceFirst("^FULL_DEFINITION: ", "");
                        view.setContainsFullDefinition(true);
                    }

                    // remove strange zero-termination seen on some Oracle view definitions
                    int length = definition.length();
                    if (definition.charAt(length-1) == 0) {
                      definition = definition.substring(0, length-1);
                    }

                    if (database instanceof InformixDatabase) {
                        // Cleanup
                        definition = definition.trim();
                        definition = definition.replaceAll("\\s*,\\s*", ", ");
                        definition = definition.replaceAll("\\s*;", "");

                        // Strip the schema definition because it can optionally be included in the tag attribute
                        definition = definition.replaceAll("(?i)\""+view.getSchema().getName()+"\"\\.", "");
                    }

                    view.setDefinition(definition);
                } catch (DatabaseException e) {
                    throw new DatabaseException("Error getting " + database.getConnection().getURL() + " view with " + new GetViewDefinitionStatement(view.getSchema().getCatalogName(), view.getSchema().getName(), rawViewName), e);
                }

                return view;
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!snapshot.getSnapshotControl().shouldInclude(View.class)) {
            return;
        }

        if (foundObject instanceof Schema) {
            Schema schema = (Schema) foundObject;
            Database database = snapshot.getDatabase();
            List<CachedRow> viewsMetadataRs = null;
            try {
                viewsMetadataRs = ((JdbcDatabaseSnapshot) snapshot).getMetaDataFromCache().getViews(((AbstractJdbcDatabase) database).getJdbcCatalogName(schema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(schema), null);
                for (CachedRow row : viewsMetadataRs) {
                    CatalogAndSchema catalogAndSchema = ((AbstractJdbcDatabase) database).getSchemaFromJdbcInfo(row.getString("TABLE_CAT"), row.getString("TABLE_SCHEM"));
                    View view = new View();
                    view.setName(row.getString("TABLE_NAME"));
                    view.setSchema(new Schema(catalogAndSchema.getCatalogName(), catalogAndSchema.getSchemaName()));
                    view.setRemarks(row.getString("REMARKS"));
                    view.setDefinition(row.getString("OBJECT_BODY"));
                    if(database instanceof OracleDatabase) {
                        view.setAttribute("editioning", "Y".equals(row.getString("EDITIONING_VIEW")));
                    }
                    schema.addDatabaseObject(view);
                }
            } catch (SQLException e) {
                throw new DatabaseException(e);
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
