package liquibase.exception;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

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