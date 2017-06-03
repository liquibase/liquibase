package liquibase.dbtest.mssql;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.exception.ValidationFailedException;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assume.assumeNotNull;

/**
 *
 * @author lujop
 */
public abstract class AbstractMssqlIntegrationTest extends AbstractIntegrationTest{

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
    public void smartDataLoad() throws Exception {
        assumeNotNull(this.getDatabase());
        Liquibase liquibase = createLiquibase("changelogs/common/smartDataLoad.changelog.xml");
        clearDatabase(liquibase);
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

//    @Override
//    //Mssql has problems with insert data in autoincrement tables. Because diff detects the id of that inserts and when it is ran the diff
//    //it tries to insert values in identity columns that isn't allowed in mssql
//    @Test(expected = MigrationFailedException.class)
//    public void testRerunDiffChangeLog() throws Exception {
//        if (getDatabase() == null) {
//            throw new MigrationFailedException();
//        }
//        super.testRerunDiffChangeLog();
//    }

}
