package liquibase.snapshot.jvm;

import java.sql.ResultSet;
import java.sql.SQLException;

import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;

public class CatalogSnapshotGenerator extends JdbcSnapshotGenerator {

    public CatalogSnapshotGenerator() {
        super(Catalog.class);
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
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
            if (((AbstractJdbcDatabase) database).jdbcCallsCatalogsSchemas()) {
                catalogs = ((JdbcConnection) database.getConnection()).getMetaData().getSchemas();
            } else {
                catalogs = ((JdbcConnection) database.getConnection()).getMetaData().getCatalogs();
            }
            while (catalogs.next()) {
                Catalog catalog;
                if (((AbstractJdbcDatabase) database).jdbcCallsCatalogsSchemas()) {
                    catalog = new Catalog(catalogs.getString("TABLE_SCHEM"));
                } else {
                    catalog = new Catalog(catalogs.getString("TABLE_CAT"));
                }

                if (DatabaseObjectComparatorFactory.getInstance().isSameObject(catalog, example, database)) {
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

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        //nothing to add to
    }
}
