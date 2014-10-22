package liquibase.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.util.JdbcUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SchemaSnapshotGenerator extends JdbcSnapshotGenerator {


    public SchemaSnapshotGenerator() {
        super(Schema.class);
    }


    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        Database database = snapshot.getDatabase();
        Schema match = null;

        String catalogName = ((Schema) example).getCatalogName();
        String schemaName = example.getName();
        if (database.supportsSchemas()) {
            if (catalogName == null) {
                catalogName = database.getDefaultCatalogName();
            }
            if (schemaName == null) {
                schemaName = database.getDefaultSchemaName();
            }
        } else {
            if (database.supportsCatalogs()) {
                if (catalogName == null && schemaName != null) {
                    catalogName = schemaName;
                    schemaName = null;
                }
            } else {
                catalogName = null;
                schemaName = null;
            }
        }

        example = new Schema(catalogName, schemaName);

        // use LEGACY quoting since we're dealing with system objects
        ObjectQuotingStrategy currentStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
        try {
            if (database.supportsSchemas()) {
                for (String tableSchema : getDatabaseSchemaNames(database)) {
                    CatalogAndSchema schemaFromJdbcInfo = toCatalogAndSchema(tableSchema, database);

                    Catalog catalog = new Catalog(schemaFromJdbcInfo.getCatalogName());

                    Schema schema = new Schema(catalog, tableSchema);
                    if (DatabaseObjectComparatorFactory.getInstance().isSameObject(schema, example, database)) {
                        if (match == null) {
                            match = schema;
                        } else {
                            throw new InvalidExampleException("Found multiple catalog/schemas matching " + ((Schema) example).getCatalogName() + "." + example.getName());
                        }
                    }
                }
            } else {
                Catalog catalog = new Catalog(catalogName);
                match = new Schema(catalog, catalogName);
            }

        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            database.setObjectQuotingStrategy(currentStrategy);
        }

        if (match != null && (match.getName() == null || match.getName().equalsIgnoreCase(database.getDefaultSchemaName()))) {
            match.setDefault(true);
        }
        return match;
    }

    protected CatalogAndSchema toCatalogAndSchema(String tableSchema, Database database) {
        return ((AbstractJdbcDatabase) database).getSchemaFromJdbcInfo(null, tableSchema);
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        //no other types
    }

    protected String[] getDatabaseSchemaNames(Database database) throws SQLException, DatabaseException {
        List<String> returnList = new ArrayList<String>();

        ResultSet schemas = null;
        try {
            schemas = ((JdbcConnection) database.getConnection()).getMetaData().getSchemas();
            while (schemas.next()) {
                returnList.add(JdbcUtils.getValueForColumn(schemas, "TABLE_SCHEM", database));
            }
        } finally {
            if (schemas != null) {
                schemas.close();
            }
        }

        return returnList.toArray(new String[returnList.size()]);
    }

}
