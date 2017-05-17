package liquibase.dbtest.mssql;

import java.util.Date;
import liquibase.Liquibase;
import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.exception.MigrationFailedException;
import liquibase.exception.ValidationFailedException;
import org.junit.Test;

import static org.junit.Assume.assumeNotNull;

/**
 *
 * @author lujop
 */
public abstract class AbstractMssqlIntegrationTest extends AbstractIntegrationTest{

    public AbstractMssqlIntegrationTest(String changelogDir, String url) throws Exception {
        super(changelogDir, url);
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
