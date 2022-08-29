package liquibase.ext.bigquery.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MariaDBDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.CachedRow;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.JdbcDatabaseSnapshot;
import liquibase.snapshot.jvm.ViewSnapshotGenerator;
import liquibase.statement.core.GetViewDefinitionStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.View;
import liquibase.util.StringUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static liquibase.ext.bigquery.database.BigqueryDatabase.BIGQUERY_PRIORITY_DATABASE;

public class BigQueryViewSnapshotGenerator extends ViewSnapshotGenerator {



    public int getPriority() {
        return BIGQUERY_PRIORITY_DATABASE;
    }


    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException {
        if (((View)example).getDefinition() != null) {
            return example;
        } else {
            Database database = snapshot.getDatabase();
            Schema schema = example.getSchema();
            //List viewsMetadataRs = null;

                //viewsMetadataRs = ((JdbcDatabaseSnapshot)snapshot).getMetaDataFromCache().getViews(((AbstractJdbcDatabase)database).getJdbcCatalogName(schema), ((AbstractJdbcDatabase)database).getJdbcSchemaName(schema), example.getName());
                CatalogAndSchema catalogAndSchema = (new CatalogAndSchema(schema.getCatalogName(), schema.getName())).customize(database);
                String jdbcSchemaName = database.correctObjectName(((AbstractJdbcDatabase)database).getJdbcSchemaName(catalogAndSchema), Schema.class);
                String query = String.format("select view_definition from "+jdbcSchemaName+"."+database.getSystemSchema().toUpperCase()+".VIEWS where table_name='%s' and table_schema='%s' and table_catalog='%s';"
                        , example.getName(), schema.getName(), schema.getCatalogName());

                //select view_definition from information_schema.views where table_name='test_view' and table_schema='lharness' and table_catalog='lolejniczakdatabricksdemo'

            List<Map<String, ?>> viewsMetadataRs = Scope.getCurrentScope().getSingleton(ExecutorService.class)
                        .getExecutor("jdbc", database).queryForList(new RawSqlStatement(query));

                if (viewsMetadataRs.isEmpty()) {
                    return null;
                } else {
                    CachedRow row = (CachedRow) viewsMetadataRs.get(0);
                    String rawViewName = row.getString("TABLE_NAME");
                    String rawSchemaName = StringUtil.trimToNull(row.getString("TABLE_SCHEM"));
                    String rawCatalogName = StringUtil.trimToNull(row.getString("TABLE_CAT"));
                    String remarks = row.getString("REMARKS");
                    if (remarks != null) {
                        remarks = remarks.replace("''", "'");
                    }

                    View view = (new View()).setName(this.cleanNameFromDatabase(rawViewName, database));
                    view.setRemarks(remarks);
                    CatalogAndSchema schemaFromJdbcInfo = ((AbstractJdbcDatabase)database).getSchemaFromJdbcInfo(rawCatalogName, rawSchemaName);
                    view.setSchema(new Schema(schemaFromJdbcInfo.getCatalogName(), schemaFromJdbcInfo.getSchemaName()));

                    try {
                        String definition = database.getViewDefinition(schemaFromJdbcInfo, view.getName());
                        if (definition.startsWith("FULL_DEFINITION: ")) {
                            definition = definition.replaceFirst("^FULL_DEFINITION: ", "");
                            view.setContainsFullDefinition(true);
                        }

                        int length = definition.length();
                        if (length > 0 && definition.charAt(length - 1) == 0) {
                            definition = definition.substring(0, length - 1);
                        }

                        if (database instanceof InformixDatabase) {
                            definition = definition.trim();
                            definition = definition.replaceAll("\\s*,\\s*", ", ");
                            definition = definition.replaceAll("\\s*;", "");
                            definition = definition.replaceAll("(?i)\"" + view.getSchema().getName() + "\"\\.", "");
                        }

                        definition = StringUtil.trimToNull(definition);
                        if (definition == null) {
                            definition = "[CANNOT READ VIEW DEFINITION]";
                            String warningMessage = null;
                            if (database instanceof MariaDBDatabase) {
                                warningMessage = "\nThe current MariaDB user does not have permissions to access view definitions needed for this Liquibase command.\nPlease search the changelog for '[CANNOT READ VIEW DEFINITION]' to locate inaccessible objects. Learn more about altering permissions with suggested MariaDB GRANTs at https://docs.liquibase.com/workflows/liquibase-pro/mariadbgrants.html\n";
                            } else if (database instanceof MySQLDatabase) {
                                warningMessage = "\nThe current MySQL user does not have permissions to access view definitions needed for this Liquibase command.\nPlease search the changelog for '[CANNOT READ VIEW DEFINITION]' to locate inaccessible objects. This is\npotentially due to a known MySQL bug https://bugs.mysql.com/bug.php?id=22763. Learn more about altering\npermissions with suggested MySQL GRANTs at https://docs.liquibase.com/workflows/liquibase-pro/mysqlgrants.html\n";
                            }

                            if (warningMessage != null) {
                                Scope.getCurrentScope().getUI().sendMessage("WARNING: " + warningMessage);
                                Scope.getCurrentScope().getLog(this.getClass()).warning(warningMessage);
                            }
                        }

                        view.setDefinition(definition);
                    } catch (DatabaseException var16) {
                        throw new DatabaseException("Error getting " + database.getConnection().getURL() + " view with " + new GetViewDefinitionStatement(view.getSchema().getCatalogName(), view.getSchema().getName(), rawViewName), var16);
                    }

                    return view;
                }

        }
    }

}
