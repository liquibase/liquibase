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

import java.sql.ResultSet;
import java.sql.SQLException;

public class CatalogSnapshotGenerator extends JdbcSnapshotGenerator {

    public CatalogSnapshotGenerator() {
        super(Catalog.class);
    }

//    public Boolean has(DatabaseObject example, DatabaseSnapshot snapshot, SnapshotGeneratorChain chain) throws DatabaseException {
//        return chain.has(example, snapshot);  //To change body of implemented methods use File | Settings | File Templates.
//    }

    public DatabaseObject snapshot(DatabaseObject example, DatabaseSnapshot snapshot, SnapshotGeneratorChain chain) throws DatabaseException, InvalidExampleException {
        if (!(example instanceof Catalog)) {
            throw new UnexpectedLiquibaseException("Unexpected example type: "+example.getClass().getName());
        }
        Database database = snapshot.getDatabase();
        ResultSet catalogs = null;
        Catalog match = null;
        String catalogName = example.getName();
        if (catalogName == null && database.supportsCatalogs()) {
            catalogName = database.getDefaultCatalogName();
        }
        example = new Catalog(catalogName);

        try {
            catalogs = ((JdbcConnection) database.getConnection()).getMetaData().getCatalogs();
            while (catalogs.next()) {
                CatalogAndSchema schemaFromJdbcInfo = database.getSchemaFromJdbcInfo(catalogs.getString("TABLE_CAT"), null);

                Catalog catalog = new Catalog(schemaFromJdbcInfo.getCatalogName());

                if (catalog.equals(example, database)) {
                    if (match == null) {
                        match = catalog;
                    } else {
                        throw new InvalidExampleException("Found multiple catalogs matching " + example.getName());
                    }
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            if (catalogs != null) {
                try {
                    catalogs.close();
                } catch (SQLException ignore) {

                }
            }
        }
        return match;
    }
}
