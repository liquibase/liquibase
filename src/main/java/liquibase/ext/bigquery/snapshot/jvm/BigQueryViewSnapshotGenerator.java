package liquibase.ext.bigquery.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.jvm.ViewSnapshotGenerator;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.View;
import liquibase.util.StringUtil;

import java.util.List;
import java.util.Map;

import static liquibase.ext.bigquery.database.BigqueryDatabase.BIGQUERY_PRIORITY_DATABASE;

public class BigQueryViewSnapshotGenerator extends ViewSnapshotGenerator {


    public int getPriority() {
        return BIGQUERY_PRIORITY_DATABASE;
    }


    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException {
        if (((View) example).getDefinition() != null) {
            return example;
        } else {
            Database database = snapshot.getDatabase();
            Schema schema = example.getSchema();

            CatalogAndSchema catalogAndSchema = (new CatalogAndSchema(schema.getCatalogName(), schema.getName())).customize(database);
            String jdbcSchemaName = database.correctObjectName(((AbstractJdbcDatabase) database).getJdbcSchemaName(catalogAndSchema), Schema.class);
            String query = String.format("SELECT view_definition FROM " + jdbcSchemaName + "." + database.getSystemSchema().toUpperCase() + ".VIEWS WHERE table_name='%s' AND table_schema='%s' AND table_catalog='%s';"
                    , example.getName(), schema.getName(), schema.getCatalogName());

            List<Map<String, ?>> viewsMetadataRs = Scope.getCurrentScope().getSingleton(ExecutorService.class)
                    .getExecutor("jdbc", database).queryForList(new RawSqlStatement(query));

            String viewDefinition = "";
            if (viewsMetadataRs.isEmpty()) {
                return null;
            } else {
                Map<String, ?> row = viewsMetadataRs.get(0);
                String rawViewName = example.getName(); //(String) row.get("VIEW_DEFINITION");
                String rawSchemaName = schema.getName(); //StringUtil.trimToNull((String) row.get("TABLE_SCHEM"));
                String rawCatalogName = schema.getCatalogName(); //StringUtil.trimToNull((String) row.get("TABLE_CAT"));
                String remarks = null;// (String) row.get("REMARKS");

                viewDefinition = (String) row.get("VIEW_DEFINITION");

                View view = (new View()).setName(this.cleanNameFromDatabase(rawViewName, database));
                view.setRemarks(remarks);
                CatalogAndSchema schemaFromJdbcInfo = ((AbstractJdbcDatabase) database).getSchemaFromJdbcInfo(rawCatalogName, rawSchemaName);
                view.setSchema(new Schema(schemaFromJdbcInfo.getCatalogName(), schemaFromJdbcInfo.getSchemaName()));

                //try {
                String definition = viewDefinition; //database.getViewDefinition(schemaFromJdbcInfo, view.getName());
                if (definition.startsWith("FULL_DEFINITION: ")) {
                    definition = definition.replaceFirst("^FULL_DEFINITION: ", "");
                    view.setContainsFullDefinition(true);
                }

                int length = definition.length();
                if (length > 0 && definition.charAt(length - 1) == 0) {
                    definition = definition.substring(0, length - 1);
                }

                definition = StringUtil.trimToNull(definition);
                if (definition == null) {
                    definition = "[CANNOT READ VIEW DEFINITION]";
                }

                view.setDefinition(definition);

                return view;
            }

        }
    }

}
