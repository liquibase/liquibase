package liquibase.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MariaDBDatabase;
import liquibase.database.core.MySQLDatabase;
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
import liquibase.util.StringUtil;

import java.sql.SQLException;
import java.util.List;

public class ViewSnapshotGenerator extends JdbcSnapshotGenerator {

    public ViewSnapshotGenerator() {
        super(View.class, new Class[] { Schema.class });
    }


    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException {
        if (((View) example).getDefinition() != null) {
            return example;
        }
        Database database = snapshot.getDatabase();
        Schema schema = example.getSchema();

        List<CachedRow> viewsMetadataRs;
        try {
            viewsMetadataRs = ((JdbcDatabaseSnapshot) snapshot).getMetaDataFromCache().getViews(((AbstractJdbcDatabase) database).getJdbcCatalogName(schema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(schema), example.getName());
            if (!viewsMetadataRs.isEmpty()) {
                CachedRow row = viewsMetadataRs.get(0);
                String rawViewName = row.getString("TABLE_NAME");
                String rawSchemaName = StringUtil.trimToNull(row.getString("TABLE_SCHEM"));
                String rawCatalogName = StringUtil.trimToNull(row.getString("TABLE_CAT"));
                String remarks = row.getString("REMARKS");
                if (remarks != null) {
                    remarks = remarks.replace("''", "'"); //come back escaped sometimes
                }

                View view = new View().setName(cleanNameFromDatabase(rawViewName, database));
                view.setRemarks(remarks);
                CatalogAndSchema schemaFromJdbcInfo = ((AbstractJdbcDatabase) database).getSchemaFromJdbcInfo(rawCatalogName, rawSchemaName);
                view.setSchema(new Schema(schemaFromJdbcInfo.getCatalogName(), schemaFromJdbcInfo.getSchemaName()));

                ObjectQuotingStrategy originalQuotingStrategy = database.getObjectQuotingStrategy();
                try {
                    database.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);
                    String definition = database.getViewDefinition(schemaFromJdbcInfo, view.getName());

                    if (definition != null && definition.startsWith("FULL_DEFINITION: ")) {
                        definition = definition.replaceFirst("^FULL_DEFINITION: ", "");
                        view.setContainsFullDefinition(true);
                    }

                    // remove strange zero-termination seen on some Oracle view definitions
                    int length = definition != null ? definition.length() : 0;
                    if (length > 0 && definition.charAt(length-1) == 0) {
                      definition = definition.substring(0, length-1);
                    }

                    if (database instanceof InformixDatabase && definition != null) {
                        // Cleanup
                        definition = definition.trim();
                        definition = definition.replaceAll("\\s*,\\s*", ", ");
                        definition = definition.replaceAll("\\s*;", "");

                        // Strip the schema definition because it can optionally be included in the tag attribute
                        definition = definition.replaceAll("(?i)\""+view.getSchema().getName()+"\"\\.", "");
                    }

                    definition = StringUtil.trimToNull(definition);
                    if (definition == null) {
                        definition = "[CANNOT READ VIEW DEFINITION]";
                        String warningMessage = null;
                        if (database instanceof MariaDBDatabase) {
                            warningMessage =
                                    "\nThe current MariaDB user does not have permissions to access view definitions needed for this Liquibase command.\n" +
                                            "Please search the changelog for '[CANNOT READ VIEW DEFINITION]' to locate inaccessible objects. " +
                                            "Learn more about altering permissions with suggested MariaDB GRANTs at https://docs.liquibase.com/workflows/liquibase-pro/mariadbgrants.html\n";

                        } else if (database instanceof MySQLDatabase) {
                            warningMessage =
                                    "\nThe current MySQL user does not have permissions to access view definitions needed for this Liquibase command.\n" +
                                    "Please search the changelog for '[CANNOT READ VIEW DEFINITION]' to locate inaccessible objects. This is\n" +
                                    "potentially due to a known MySQL bug https://bugs.mysql.com/bug.php?id=22763. Learn more about altering\n" +
                                    "permissions with suggested MySQL GRANTs at https://docs.liquibase.com/workflows/liquibase-pro/mysqlgrants.html\n";
                        }
                        if (warningMessage != null) {
                            Scope.getCurrentScope().getUI().sendMessage("WARNING: " + warningMessage);
                            Scope.getCurrentScope().getLog(getClass()).warning(warningMessage);
                        }
                    }

                    view.setDefinition(definition);
                } catch (DatabaseException e) {
                    throw new DatabaseException("Error getting " + database.getConnection().getURL() + " view with " + new GetViewDefinitionStatement(view.getSchema().getCatalogName(), view.getSchema().getName(), rawViewName), e);
                } finally {
                    database.setObjectQuotingStrategy(originalQuotingStrategy);
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
            List<CachedRow> viewsMetadataRs;
            try {
                viewsMetadataRs = ((JdbcDatabaseSnapshot) snapshot).getMetaDataFromCache().getViews(((AbstractJdbcDatabase) database).getJdbcCatalogName(schema), ((AbstractJdbcDatabase) database).getJdbcSchemaName(schema), null);
                for (CachedRow row : viewsMetadataRs) {
                    CatalogAndSchema catalogAndSchema = ((AbstractJdbcDatabase) database).getSchemaFromJdbcInfo(row.getString("TABLE_CAT"), row.getString("TABLE_SCHEM"));
                    View view = new View();
                    view.setName(row.getString("TABLE_NAME"));
                    view.setSchema(new Schema(catalogAndSchema.getCatalogName(), catalogAndSchema.getSchemaName()));
                    view.setRemarks(row.getString("REMARKS"));
                    String definition = StringUtil.standardizeLineEndings(row.getString("OBJECT_BODY"));
                    view.setDefinition(definition);
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
}
