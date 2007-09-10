package liquibase.migrator;

import liquibase.exception.DuplicateChangeSetException;
import static org.junit.Assert.assertEquals;
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