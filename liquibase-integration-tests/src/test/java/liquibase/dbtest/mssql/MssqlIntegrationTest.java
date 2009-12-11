package liquibase.dbtest.mssql;

import liquibase.Liquibase;
import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.exception.ValidationFailedException;
import org.junit.Test;

import java.util.Date;

public class MssqlIntegrationTest extends AbstractIntegrationTest {

    public MssqlIntegrationTest() throws Exception {
        super("mssql", "jdbc:sqlserver://"+ getDatabaseServerHostname() +":1433;instanceName=SQLEXPRESS2005;databaseName=liquibase");
    }

    @Override
    protected boolean shouldRollBack() {
        return false;
    }

    @Test
    public void smartDataLoad() throws Exception {
//         if (this.getDatabase() == null) {
//            return;
//        }

        Liquibase liquibase = createLiquibase("changelogs/common/smartDataLoad.changelog.xml");
        clearDatabase(liquibase);

        try {
            liquibase.update(this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }

        // check that the automatically rollback now works too
        try {
            liquibase.rollback( new Date(0),this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }
    }
    
}
