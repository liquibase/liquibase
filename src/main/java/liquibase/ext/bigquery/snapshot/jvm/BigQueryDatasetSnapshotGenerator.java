package liquibase.ext.bigquery.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.exception.DatabaseException;
import liquibase.ext.bigquery.database.BigqueryDatabase;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.jvm.SchemaSnapshotGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import liquibase.util.JdbcUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BigQueryDatasetSnapshotGenerator extends SchemaSnapshotGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        int priority = super.getPriority(objectType, database);
        if (database instanceof BigqueryDatabase) {
            priority += PRIORITY_DATABASE;
        }
        return priority;
    }

    @Override
    protected String[] getDatabaseSchemaNames(Database database) throws SQLException, DatabaseException {
        List<String> returnList = new ArrayList<>();

        ResultSet schemas = null;
        try {
            System.out.println("Getting BigQuery datasets");
            System.out.println("Default catalog name: "+database.getDefaultCatalogName());
            System.out.println("Default schema: "+database.getDefaultSchemaName());
            schemas = ((JdbcConnection) database.getConnection()).getMetaData()
                    .getSchemas(
                            database.getDefaultCatalogName(),
                            null //database.getDefaultSchemaName()
                    );

            ResultSetMetaData rsmd = schemas.getMetaData();
            int columnCount = rsmd.getColumnCount();

            for (int i = 1; i <= columnCount; i++ ) {
                String name = rsmd.getColumnName(i);
                System.out.println(name);
                // Do stuff with name
            }

            int j = 0;
            while (schemas.next()) {
                System.out.println(j+" "+schemas.getString("TABLE_CATALOG")+" "+schemas.getString("TABLE_SCHEM"));
                returnList.add(JdbcUtils.getValueForColumn(schemas, "TABLE_SCHEM", database));
            }
        } finally {
            if (schemas != null) {
                schemas.close();
            }
        }

        return returnList.toArray(new String[returnList.size()]);
    }

@Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        Database database = snapshot.getDatabase();
        Schema match = null;
        String catalogName = ((Schema)example).getCatalogName();
        String schemaName = example.getName();
        if (database.supportsSchemas()) {
            if (catalogName == null) {
                catalogName = database.getDefaultCatalogName();
            }

            if (schemaName == null) {
                schemaName = database.getDefaultSchemaName();
            }
        } else if (database.supportsCatalogs()) {
            if (catalogName == null && schemaName != null) {
                catalogName = schemaName;
                schemaName = null;
            }
        } else {
            catalogName = null;
            schemaName = null;
        }

        DatabaseObject example1 = new Schema(catalogName, schemaName);
        ObjectQuotingStrategy currentStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);

        try {
            if (database.supportsSchemas()) {
                String[] var8 = this.getDatabaseSchemaNames(database);
                int var9 = var8.length;

                for(int var10 = 0; var10 < var9; ++var10) {
                    String tableSchema = var8[var10];
                    CatalogAndSchema schemaFromJdbcInfo = this.toCatalogAndSchema(tableSchema, database);
                    Catalog catalog = new Catalog(schemaFromJdbcInfo.getCatalogName());
                    Schema schema = new Schema(catalog, tableSchema);
                    if (DatabaseObjectComparatorFactory.getInstance().isSameObject(schema, example1, snapshot.getSchemaComparisons(), database)) {
                        if (match != null) {
                            throw new InvalidExampleException("Found multiple catalog/schemas matching " + ((Schema)example).getCatalogName() + "." + example.getName());
                        }

                        match = schema;
                    }
                }
            } else if (((Schema)example1).getCatalog().isDefault()) {
                match = new Schema(((Schema)example1).getCatalog(), catalogName);
            } else {
                Catalog catalog = ((Schema)example1).getCatalog();
                String[] dbCatalogNames = this.getDatabaseCatalogNames(database);
                String[] var23 = dbCatalogNames;
                int var24 = dbCatalogNames.length;

                for(int var25 = 0; var25 < var24; ++var25) {
                    String candidateCatalogName = var23[var25];
                    if (catalog.equals(new Catalog(candidateCatalogName))) {
                        match = new Schema(catalog, catalogName);
                    }
                }
            }
        } catch (SQLException var18) {
            throw new DatabaseException(var18);
        } finally {
            database.setObjectQuotingStrategy(currentStrategy);
        }

        if (match != null && (match.getName() == null || match.getName().equalsIgnoreCase(database.getDefaultSchemaName()))) {
            match.setDefault(true);
        }

        return match;
    }

}
