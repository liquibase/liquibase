package liquibase.migrator.change;

import junit.framework.TestCase;

public class ConstraintsConfigTest extends TestCase {

    public void testConstraints() throws Exception {
        ConstraintsConfig constraints = new ConstraintsConfig();
        assertNull(constraints.isDeleteCascade());
        assertNull(constraints.isInitiallyDeferred());
        assertNull(constraints.isNullable());
        assertNull(constraints.isPrimaryKey());
        assertNull(constraints.isUnique());
    }
}