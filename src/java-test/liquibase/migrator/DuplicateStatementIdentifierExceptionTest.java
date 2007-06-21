package liquibase.migrator;

import static org.junit.Assert.assertEquals;
import liquibase.migrator.exception.DuplicateStatementIdentifierException;

import org.junit.Test;

/**
 * Tests for {@link DuplicateStatementIdentifierException}
 */
public class DuplicateStatementIdentifierExceptionTest {

    @Test
    public void duplicateStatementIdentifierException() throws Exception {
        DuplicateStatementIdentifierException ex = new DuplicateStatementIdentifierException("Message Here");
        assertEquals("Message Here", ex.getMessage());
    }
}