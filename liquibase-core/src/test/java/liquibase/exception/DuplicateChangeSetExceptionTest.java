package liquibase.exception;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests for {@link DuplicateChangeSetException}
 */
@SuppressWarnings({"ThrowableInstanceNeverThrown"})
public class DuplicateChangeSetExceptionTest {

    @Test
    public void duplicateChangeSetException() throws Exception {
        DuplicateChangeSetException duplicateChangeSetException = new DuplicateChangeSetException("MESSAGE HERE");
        assertEquals("MESSAGE HERE", duplicateChangeSetException.getMessage());
    }
}