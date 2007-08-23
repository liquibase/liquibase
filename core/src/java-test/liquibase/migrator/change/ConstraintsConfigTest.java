package liquibase.migrator.change;

import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 * Tests for {@link ConstraintsConfig}
 */
public class ConstraintsConfigTest {

    @Test
    public void constraints() throws Exception {
        ConstraintsConfig constraints = new ConstraintsConfig();
        assertNull(constraints.isDeleteCascade());
        assertNull(constraints.isInitiallyDeferred());
        assertNull(constraints.isNullable());
        assertNull(constraints.isPrimaryKey());
        assertNull(constraints.isUnique());
    }
}