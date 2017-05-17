package liquibase.dbtest.firebird;

import liquibase.CatalogAndSchema;
import liquibase.snapshot.SnapshotControl;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.exception.DatabaseException;

/**
 * To configure database:
 * create database 'c:\firebird\liquibase.fdb' page_size 8192
 * user 'liquibase' password 'liquibase';
 *
 * connecting later:
 * connect '\firebird\liquibase.fdb' USER 'liquibase' PASSWORD 'liquibase';
 */
public class FirebirdIntegrationTest extends AbstractIntegrationTest {

    public FirebirdIntegrationTest() throws Exception {
        super("firebird", "jdbc:firebirdsql:"+ getDatabaseServerHostname("Firebird") +"/3050:c:\\firebird\\liquibase.fdb");
    }

    @Override
    protected boolean isDatabaseProvidedByTravisCI() {
        // Seems unlikely to ever be provided by Travis, as it's not free
        return false;
    }

    @Override
    protected boolean shouldRollBack() {
        return false;
    }

    @Override
    protected CatalogAndSchema[] getSchemasToDrop() throws DatabaseException {
        return new CatalogAndSchema[] {
                CatalogAndSchema.DEFAULT
        };
    }

}
