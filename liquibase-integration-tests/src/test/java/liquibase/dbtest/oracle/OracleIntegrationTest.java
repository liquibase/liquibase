package liquibase.dbtest.oracle;

import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationFailedException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogService;
import liquibase.logging.Logger;
import liquibase.sql.visitor.AbstractSqlVisitor;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.core.DropTableStatement;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;

/**
 * Integration test for Oracle Database, Version 11gR2 and above.
 */
public class OracleIntegrationTest extends AbstractIntegrationTest {
    String indexOnSchemaChangeLog;
    String viewOnSchemaChangeLog;
    String customExecutorChangeLog;
    Logger logger = LogService.getLog(OracleIntegrationTest.class);

    public OracleIntegrationTest() throws Exception {
        super("oracle", DatabaseFactory.getInstance().getDatabase("oracle"));
         indexOnSchemaChangeLog = "changelogs/oracle/complete/indexOnSchema.xml";
         viewOnSchemaChangeLog = "changelogs/oracle/complete/viewOnSchema.xml";
         customExecutorChangeLog = "changelogs/oracle/complete/sqlplusExecutor.xml";
        // Respect a user-defined location for sqlnet.ora, tnsnames.ora etc. stored in the environment
        // variable TNS_ADMIN. This allowes the use of TNSNAMES.
        if (System.getenv("TNS_ADMIN") != null)
            System.setProperty("oracle.net.tns_admin",System.getenv("TNS_ADMIN"));
    }

    @Override
    protected boolean isDatabaseProvidedByTravisCI() {
        // Seems unlikely to ever be provided by Travis, as it's not free
        return false;
    }

    @Override
    @Test
    public void testRunChangeLog() throws Exception {
        super.testRunChangeLog();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Test
    public void sqlplusChangelog() throws Exception {
        Database database = this.getDatabase();
        assumeNotNull(database);

        Liquibase liquibase = createLiquibase(this.customExecutorChangeLog);
        clearDatabase();

        //
        // Add a visitor so we can assert
        //
        DatabaseChangeLog changeLog = liquibase.getDatabaseChangeLog();
        for (ChangeSet changeSet : changeLog.getChangeSets()) {
            changeSet.addSqlVisitor(new TestSqlVisitor());
        }
        try {
            liquibase.update(this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }
        database.commit();
    }

    private class TestSqlVisitor extends AbstractSqlVisitor {
        @Override
        public String modifySql(String sql, Database database) {
            logger.info("In the TestSqlVisitor.modifySql method");
            logger.info(sql);
            assertTrue(sql.startsWith("CREATE TABLE primary_table_numero_uno (name CHAR(20));"));
            assertTrue(sql.endsWith("CREATE TABLE primary_table_numero_cinco(name CHAR(20));"));
            return null;
        }

        @Override
        public String getName() {
            return null;
        }
    }

    @Test
    public void indexCreatedOnCorrectSchema() throws Exception {
        assumeNotNull(this.getDatabase());

        Liquibase liquibase = createLiquibase(this.indexOnSchemaChangeLog);
        clearDatabase();

        try {
            liquibase.update(this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }

        Statement queryIndex = ((JdbcConnection) this.getDatabase().getConnection()).getUnderlyingConnection().createStatement();

        ResultSet indexOwner = queryIndex.executeQuery("SELECT owner FROM ALL_INDEXES WHERE index_name = 'IDX_BOOK_ID'");

        assertTrue(indexOwner.next());

        String owner = indexOwner.getString("owner");

        assertEquals("LBCAT2", owner);

        // check that the automatically rollback now works too
        try {
            liquibase.rollback( new Date(0),this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }




    }

    @Test
    public void viewCreatedOnCorrectSchema() throws Exception {
        assumeNotNull(this.getDatabase());

        Liquibase liquibase = createLiquibase(this.viewOnSchemaChangeLog);
        clearDatabase();

        try {
            liquibase.update(this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }

        Statement queryIndex = ((JdbcConnection) this.getDatabase().getConnection()).getUnderlyingConnection().createStatement();

        ResultSet indexOwner = queryIndex.executeQuery("SELECT owner FROM ALL_VIEWS WHERE view_name = 'V_BOOK2'");

        assertTrue(indexOwner.next());

        String owner = indexOwner.getString("owner");

        assertEquals("LBCAT2", owner);

        // check that the automatically rollback now works too
        try {
            liquibase.rollback( new Date(0),this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }
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

        // check that the automatically rollback now works too
        try {
            liquibase.rollback( new Date(0),this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }
    }

    @Override
    @Test
    public void testDiffExternalForeignKeys() throws Exception {
        //cross-schema security for oracle is a bother, ignoring test for now
    }
}