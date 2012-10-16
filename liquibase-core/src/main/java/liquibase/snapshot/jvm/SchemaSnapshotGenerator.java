package liquibase.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.snapshot.SnapshotGeneratorChain;
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

//    public Boolean has(DatabaseObject example, DatabaseSnapshot snapshot, SnapshotGeneratorChain chain) throws DatabaseException {
//        return chain.has(example, snapshot);
//    }

    public DatabaseObject snapshot(DatabaseObject example, DatabaseSnapshot snapshot, SnapshotGeneratorChain chain) throws DatabaseException, InvalidExampleException {
        if (example instanceof Schema) {
            Schema exampleSchema = (Schema) example;
            Database database = snapshot.getDatabase();
            ResultSet schemas = null;
            Schema match = null;
            String catalogName = exampleSchema.getCatalogName();
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
                        CatalogAndSchema schemaFromJdbcInfo = database.getSchemaFromJdbcInfo(schemas.getString("TABLE_CATALOG"), schemas.getString("TABLE_SCHEM"));

                        Catalog catalog = snapshot.snapshot(new Catalog(schemaFromJdbcInfo.getCatalogName()));

                        Schema schema = new Schema(catalog, schemaFromJdbcInfo.getSchemaName());
                        if (schema.equals(example, database)) {
                            if (match == null) {
                                match = schema;
                            } else {
                                throw new InvalidExampleException("Found multiple catalog/schemas matching " + exampleSchema.getCatalogName() + "." + example.getName());
                            }
                        }
                    }
                } else {
                    Catalog catalog = snapshot.snapshot(new Catalog(catalogName));
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
        throw new UnexpectedLiquibaseException("Unexpected example type: " + example.getClass().getName());

    }
}
