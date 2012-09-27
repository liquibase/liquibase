package liquibase.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.statement.core.GetViewDefinitionStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.View;
import liquibase.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ViewSnapshotGenerator extends JdbcDatabaseObjectSnapshotGenerator<View> {
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean has(View example, Database database) throws DatabaseException {
        String viewName = example.getName();
        Schema schema = example.getSchema();
        try {
            ResultSet rs = getMetaData(database).getTables(database.getJdbcCatalogName(schema), database.getJdbcSchemaName(schema), database.correctObjectName(viewName, View.class), new String[]{"VIEW"});
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

    public View[] get(DatabaseObject container, Database database) throws DatabaseException {
        if (!(container instanceof Schema)) {
            return new View[0];
        }

        updateListeners("Reading views for " + database.toString() + " ...");
        Schema schema = (Schema) container;

        List<View> returnList = new ArrayList<View>();
        ResultSet viewsMetadataRs = null;
        try {
            viewsMetadataRs = getMetaData(database).getTables(database.getJdbcCatalogName(schema), database.getJdbcSchemaName(schema), null, new String[]{"VIEW"});
            while (viewsMetadataRs.next()) {
                returnList.add(readView(viewsMetadataRs, database));
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

        return returnList.toArray(new View[returnList.size()]);
    }

    public View snapshot(View example, Database database) throws DatabaseException {
        Schema schema = example.getSchema();

        String objectName = example.getName();

        ResultSet viewsMetadataRs = null;
        try {
            viewsMetadataRs = getMetaData(database).getTables(database.getJdbcCatalogName(schema), database.getJdbcSchemaName(schema), database.correctObjectName(objectName, View.class), new String[]{"VIEW"});
            if (viewsMetadataRs.next()) {
                return readView(viewsMetadataRs, database);
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

    protected View readView(ResultSet viewMetadataResultSet, Database database) throws SQLException, DatabaseException {
        String rawViewName = viewMetadataResultSet.getString("TABLE_NAME");
        String rawSchemaName = StringUtils.trimToNull(viewMetadataResultSet.getString("TABLE_SCHEM"));
        String rawCatalogName = StringUtils.trimToNull(viewMetadataResultSet.getString("TABLE_CAT"));
        String remarks = viewMetadataResultSet.getString("REMARKS");

        View view = new View().setName(cleanNameFromDatabase(rawViewName, database));
        view.setRemarks(remarks);
        view.setDatabase(database);
        view.setRawSchemaName(rawSchemaName);
        view.setRawCatalogName(rawCatalogName);

        CatalogAndSchema schemaFromJdbcInfo = database.getSchemaFromJdbcInfo(rawSchemaName, rawCatalogName);
        view.setSchema(new Schema(schemaFromJdbcInfo.getCatalogName(), schemaFromJdbcInfo.getSchemaName()));

        try {
            view.setDefinition(database.getViewDefinition(schemaFromJdbcInfo, view.getName()));
        } catch (DatabaseException e) {
            throw new DatabaseException("Error getting " + database.getConnection().getURL() + " view with " + new GetViewDefinitionStatement(view.getSchema().getCatalog().getName(), view.getSchema().getName(), rawViewName), e);
        }

        return view;
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
