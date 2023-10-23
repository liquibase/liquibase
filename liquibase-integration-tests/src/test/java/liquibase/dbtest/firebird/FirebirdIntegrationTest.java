package liquibase.dbtest.firebird;

import liquibase.CatalogAndSchema;
import liquibase.database.DatabaseFactory;
import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.exception.DatabaseException;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

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

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        getDatabase().close();
    }

    @Test
    @Override
    @Ignore
    public void testTableExistsPreconditionTableNameMatch() throws Exception {
    }

    @Test
    @Override
    @Ignore
    public void testRunUpdateOnOldChangelogTableFormat() throws Exception {
    }

    @Test
    @Override
    @Ignore
    public void testOutputChangeLog() throws Exception {}

    @Test
    @Override
    @Ignore
    public void testSnapshotReportsAllObjectTypes() throws Exception {}


}
