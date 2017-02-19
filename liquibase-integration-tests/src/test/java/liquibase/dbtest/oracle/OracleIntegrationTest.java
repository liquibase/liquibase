package liquibase.dbtest.oracle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import liquibase.CatalogAndSchema;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationFailedException;

/**
 * create tablespace liquibase2 datafile 'C:\ORACLEXE\ORADATA\XE\LIQUIBASE2.DBF' SIZE 5M autoextend on next 5M
 */
public class OracleIntegrationTest extends AbstractIntegrationTest {

    private String indexOnSchemaChangeLog;
    private String viewOnSchemaChangeLog;

    public OracleIntegrationTest() throws Exception {
        super("oracle", "jdbc:oracle:thin:@" + getDatabaseServerHostname("Oracle") + ":1521:XE");
        this.indexOnSchemaChangeLog = "changelogs/oracle/complete/indexOnSchema.xml";
        this.viewOnSchemaChangeLog = "changelogs/oracle/complete/viewOnSchema.xml";
    }

    @Override
    protected CatalogAndSchema[] getSchemas() {
        List<CatalogAndSchema> schemas = new ArrayList<CatalogAndSchema>(Arrays.asList(super.getSchemas()));
        schemas.addAll(Arrays.asList(
                new CatalogAndSchema(null, "LBCAT2"),
                new CatalogAndSchema(null, "LIQUIBASE"),
                new CatalogAndSchema(null, "LIQUIBASEB")
        ));
        return schemas.toArray(new CatalogAndSchema[0]);
    }

    protected CatalogAndSchema[] getSchemasToDrop() throws DatabaseException {
        List<CatalogAndSchema> schemasToDrop = new ArrayList<CatalogAndSchema>(Arrays.asList(super.getSchemasToDrop()));
        schemasToDrop.addAll(Arrays.asList(this.getSchemas()));
        return schemasToDrop.toArray(new CatalogAndSchema[0]);
    }




    @Override
    @Test
    public void testRunChangeLog() throws Exception {
        super.testRunChangeLog();    //To change body of overridden methods use File | Settings | File Templates.
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

        assertEquals("LBCAT2",owner);

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

        assertEquals("LBCAT2",owner);

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

    @Override
    @Test
    public void testDiffExternalForeignKeys() throws Exception {
        //cross-schema security for oracle is a bother, ignoring test for now
    }
}