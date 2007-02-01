package liquibase.migrator;

import junit.framework.TestCase;

public class DuplicateStatementIdentifierExceptionTest extends TestCase {

    public void testDuplicateStatementIdentifierException() throws Exception {
        DuplicateStatementIdentifierException ex = new DuplicateStatementIdentifierException("Message Here");
        assertEquals("Message Here", ex.getMessage());
    }
}