package liquibase.dbtest.firebird;

import liquibase.dbtest.AbstractSimpleChangeLogRunnerTest;
import liquibase.exception.JDBCException;

/**
 * To configure database:
 * create database 'c:\firebird\liquibase.fdb' page_size 8192;
 * user 'liquibase' password 'liquibase';
 *
 * connecting later:
 * connect '\firebird\liquibase.fdb' USER 'liquibase' PASSWORD 'liquibase';
 */
@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class FirebirdSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public FirebirdSampleChangeLogRunnerTest() throws Exception {
        super("firebird", "jdbc:firebirdsql:localhost/3050:c:\\firebird\\liquibase.fdb");
    }

    protected boolean shouldRollBack() {
        return false;
    }

    protected String[] getSchemasToDrop() throws JDBCException {
        return new String[] {
                null,
        };
    }

}
