package liquibase.snapshot.jvm;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.util.JdbcUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
        if (catalogName == null && database.supportsCatalogs()) {
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

        if (match != null && isDefaultCatalog(match, database)) {
            match.setDefault(true);
        }
        return match;
    }

    protected boolean isDefaultCatalog(Catalog match, Database database) {
        if (CatalogAndSchema.CatalogAndSchemaCase.ORIGINAL_CASE.equals(database.getSchemaAndCatalogCase())) {
            return (match.getName() == null || match.getName().equals(database.getDefaultCatalogName()));
        }
        return (match.getName() == null || match.getName().equalsIgnoreCase(database.getDefaultCatalogName()));
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        //nothing to add to
    }

    protected String[] getDatabaseCatalogNames(Database database) throws SQLException, DatabaseException {
        List<String> returnList = new ArrayList<String>();

        ResultSet catalogs = null;

        try {
            if (((AbstractJdbcDatabase) database).jdbcCallsCatalogsSchemas()) {
                catalogs = ((JdbcConnection) database.getConnection()).getMetaData().getSchemas();
            } else {
                catalogs = ((JdbcConnection) database.getConnection()).getMetaData().getCatalogs();
            }
            while (catalogs.next()) {
                if (((AbstractJdbcDatabase) database).jdbcCallsCatalogsSchemas()) {
                    returnList.add(catalogs.getString("TABLE_SCHEM"));
                } else {
                    returnList.add(catalogs.getString("TABLE_CAT"));
                }
            }
        } finally {
            if (catalogs != null) {
                try {
                    catalogs.close();
                } catch (SQLException ignore) {

                }
            }

        }
        return returnList.toArray(new String[returnList.size()]);
    }

}
