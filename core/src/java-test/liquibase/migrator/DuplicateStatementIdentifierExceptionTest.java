package liquibase.migrator;

import liquibase.exception.DuplicateStatementIdentifierException;
import static org.junit.Assert.assertEquals;
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