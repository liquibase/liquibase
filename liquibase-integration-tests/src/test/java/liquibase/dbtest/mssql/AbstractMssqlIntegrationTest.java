package liquibase.dbtest.mssql;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.exception.ValidationFailedException;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeNotNull;

/**
 * Template for different Microsoft SQL integration tests (regular, case-sensitive etc.)
 * @author lujop
 */
public abstract class AbstractMssqlIntegrationTest extends AbstractIntegrationTest {

    public AbstractMssqlIntegrationTest(String changelogDir, Database dbms) throws Exception {
        super(changelogDir, dbms);
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

    @Test
    public void impossibleDefaultSchema() {
        Exception caughtException = null;
        try {
            getDatabase().setDefaultSchemaName("lbuser");
        } catch (Exception ex) {
            caughtException = ex;
        }
        assertNotNull("Must not allow using a defaultSchemaName that is different from the DB user's login schema.",
            caughtException);

    }

    @Test
    public void smartDataLoad() throws Exception {
        assumeNotNull(this.getDatabase());
        Liquibase liquibase = createLiquibase("changelogs/common/smartDataLoad.changelog.xml");
        clearDatabase();
        try {
            liquibase.update(this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }
        try {
            liquibase.rollback(new Date(0), this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }
    }

}
