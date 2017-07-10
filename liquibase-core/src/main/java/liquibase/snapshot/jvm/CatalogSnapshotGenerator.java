package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;

import java.sql.SQLException;

public class CatalogSnapshotGenerator extends JdbcSnapshotGenerator {

    public CatalogSnapshotGenerator() {
        super(Catalog.class);
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!(example instanceof Catalog)) {
            throw new UnexpectedLiquibaseException("Unexpected example type: " + example.getClass().getName());
        }
        Database database = snapshot.getDatabase();
        Catalog match = null;
        String catalogName = example.getName();
        if ((catalogName == null) && database.supportsCatalogs()) {
            catalogName = database.getDefaultCatalogName();
        }
        example = new Catalog(catalogName);

        try {
            for (String potentialCatalogName : getDatabaseCatalogNames(database)) {
                Catalog catalog = new Catalog(potentialCatalogName);
                if (DatabaseObjectComparatorFactory.getInstance().isSameObject(catalog, example, snapshot.getSchemaComparisons(), database)) {
                    if (match == null) {
                        match = catalog;
                    } else {
                        throw new InvalidExampleException("Found multiple catalogs matching " + example.getName());
                    }
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException(e);
        }

        if ((match != null) && isDefaultCatalog(match, database)) {
            match.setDefault(true);
        }
        return match;
    }

    protected boolean isDefaultCatalog(Catalog match, Database database) {
        return ((match.getName() == null) || match.getName().equalsIgnoreCase(database.getDefaultCatalogName()));
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        //nothing to add to
    }
    
}
