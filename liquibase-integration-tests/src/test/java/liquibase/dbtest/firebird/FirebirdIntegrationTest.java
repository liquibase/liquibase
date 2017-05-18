package liquibase.dbtest.firebird;

import liquibase.CatalogAndSchema;
import liquibase.database.DatabaseFactory;
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
        super("firebird", DatabaseFactory.getInstance().getDatabase("firebird"));
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
