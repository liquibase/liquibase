package liquibase.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SchemaSnapshotGenerator extends JdbcSnapshotGenerator {


    public SchemaSnapshotGenerator() {
        super(Schema.class);
    }


    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        Database database = snapshot.getDatabase();
        ResultSet schemas = null;
        Schema match = null;
        String catalogName = ((Schema) example).getCatalogName();
        if (catalogName == null && database.supportsCatalogs()) {
            catalogName = database.getDefaultCatalogName();
        }
        String schemaName = example.getName();
        if (schemaName == null && database.supportsCatalogs()) {
            schemaName = database.getDefaultSchemaName();
        }
        example = new Schema(catalogName, schemaName);

        try {
            if (database.supportsSchemas()) {
                schemas = ((JdbcConnection) database.getConnection()).getMetaData().getSchemas();
                while (schemas.next()) {
                    CatalogAndSchema schemaFromJdbcInfo = ((AbstractJdbcDatabase) database).getSchemaFromJdbcInfo(schemas.getString("TABLE_CATALOG"), schemas.getString("TABLE_SCHEM"));

                    Catalog catalog = snapshot.include(new Catalog(schemaFromJdbcInfo.getCatalogName()));

                    Schema schema = new Schema(catalog, schemaFromJdbcInfo.getSchemaName());
                    if (schema.equals(example, database)) {
                        if (match == null) {
                            match = schema;
                        } else {
                            throw new InvalidExampleException("Found multiple catalog/schemas matching " + ((Schema) example).getCatalogName() + "." + example.getName());
                        }
                    }
                }
            } else {
                Catalog catalog = snapshot.include(new Catalog(catalogName));
                return new Schema(catalog, null);
            }

        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            if (schemas != null) {
                try {
                    schemas.close();
                } catch (SQLException ignore) {

                }
            }
        }
        return match;
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        //no other types
    }
}
