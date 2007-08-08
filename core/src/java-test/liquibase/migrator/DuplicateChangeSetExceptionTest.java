package liquibase.migrator;

import static org.junit.Assert.assertEquals;
import liquibase.migrator.exception.DuplicateChangeSetException;

import org.junit.Test;

/**
 * Tests for {@link DuplicateChangeSetException}
 */
public class DuplicateChangeSetExceptionTest {

    @Test
    public void duplicateChangeSetException() throws Exception {
        DuplicateChangeSetException duplicateChangeSetException = new DuplicateChangeSetException("MESSAGE HERE");
        assertEquals("MESSAGE HERE", duplicateChangeSetException.getMessage());
    }
}