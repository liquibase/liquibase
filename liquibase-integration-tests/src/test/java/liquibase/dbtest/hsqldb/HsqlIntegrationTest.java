package liquibase.dbtest.hsqldb;

import liquibase.dbtest.AbstractIntegrationTest;
import org.junit.internal.AssumptionViolatedException;

public class HsqlIntegrationTest extends AbstractIntegrationTest {

    public HsqlIntegrationTest() throws Exception {
        super("hsqldb", "jdbc:hsqldb:mem:liquibase");
    }

    @Override
    protected boolean isDatabaseProvidedByTravisCI() {
        // Hsqldb is an in-process database
        return true;
    }

    @Override
    public void testRerunDiffChangeLog() throws Exception {
        throw new AssumptionViolatedException(
                "Skipping: FIXME CORE-3063: 'Database hsqldb does not support adding function-based default values'");
    }

    @Override
    public void generateChangeLog_noChanges() throws Exception {
        throw new AssumptionViolatedException(
                "Skipping: FIXME CORE-3063: 'Database hsqldb does not support adding function-based default values'");
    }

    @Override
    public void testTag() throws Exception {
        throw new AssumptionViolatedException(
                "Skipping: FIXME CORE-3063: 'Database hsqldb does not support adding function-based default values'");
    }

    @Override
    public void testUpdateTwice() throws Exception {
        throw new AssumptionViolatedException(
                "Skipping: FIXME CORE-3063: 'Database hsqldb does not support adding function-based default values'");
    }

    @Override
    public void testDbDoc() throws Exception {
        throw new AssumptionViolatedException(
                "Skipping: FIXME CORE-3063: 'Database hsqldb does not support adding function-based default values'");
    }

    @Override
    public void testDiff() throws Exception {
        throw new AssumptionViolatedException(
                "Skipping: FIXME CORE-3063: 'Database hsqldb does not support adding function-based default values'");
    }

    @Override
    public void testUpdateClearUpdate() throws Exception {
        throw new AssumptionViolatedException(
                "Skipping: FIXME CORE-3063: 'Database hsqldb does not support adding function-based default values'");
    }

    @Override
    public void runUpdateOnOldChangelogTableFormat() throws Exception {
        throw new AssumptionViolatedException(
                "Skipping: FIXME CORE-3063: 'Database hsqldb does not support adding function-based default values'");
    }

    @Override
    public void testRerunDiffChangeLogAltSchema() throws Exception {
        throw new AssumptionViolatedException(
                "Skipping: FIXME CORE-3063: 'invalid schema name: LBCAT2'");
    }

    @Override
    public void testDiffExternalForeignKeys() throws Exception {
        throw new AssumptionViolatedException(
                "Skipping: FIXME CORE-3063: 'invalid schema name: LBCAT2'");
    }

    @Override
    public void testUnrunChangeSetsEmptyDatabase() throws Exception {
        throw new AssumptionViolatedException(
                "Skipping: FIXME CORE-3063: 'Database hsqldb does not support adding function-based default values'");
    }

    @Override
    public void testOutputChangeLog() throws Exception {
        throw new AssumptionViolatedException(
                "Skipping: FIXME CORE-3063: 'Database hsqldb does not support adding function-based default values'");
    }

    @Override
    public void testClearChecksums() throws Exception {
        throw new AssumptionViolatedException(
                "Skipping: FIXME CORE-3063: 'Database hsqldb does not support adding function-based default values'");
    }

    @Override
    public void testRunChangeLog() throws Exception {
        throw new AssumptionViolatedException(
                "Skipping: FIXME CORE-3063: 'Database hsqldb does not support adding function-based default values'");
    }
}
