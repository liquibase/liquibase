package liquibase.dbtest.oracle;

import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.Liquibase;
import liquibase.database.JdbcConnection;
import liquibase.exception.ValidationFailedException;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Date;

/**
 * create tablespace liquibase2 datafile 'C:\ORACLEXE\ORADATA\XE\LIQUIBASE2.DBF' SIZE 5M autoextend on next 5M
 */
public class OracleIntegrationTest extends AbstractIntegrationTest {

    private String indexOnSchemaChangeLog;
    private String viewOnSchemaChangeLog;

    public OracleIntegrationTest() throws Exception {
        super("oracle", "jdbc:oracle:thin:@" + getDatabaseServerHostname("Oracle") + "/XE");
        this.indexOnSchemaChangeLog = "changelogs/oracle/complete/indexOnSchema.xml";
        this.viewOnSchemaChangeLog = "changelogs/oracle/complete/viewOnSchema.xml";
    }

    @Test
    public void indexCreatedOnCorrectSchema() throws Exception {
         if (this.getDatabase() == null) {
            return;
        }

        Liquibase liquibase = createLiquibase(this.indexOnSchemaChangeLog);
        clearDatabase(liquibase);

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

        assertEquals("LIQUIBASEB",owner);

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
         if (this.getDatabase() == null) {
            return;
        }

        Liquibase liquibase = createLiquibase(this.viewOnSchemaChangeLog);
        clearDatabase(liquibase);

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

        assertEquals("LIQUIBASEB",owner);

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
        if (this.getDatabase() == null) {
            return;
        }

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
