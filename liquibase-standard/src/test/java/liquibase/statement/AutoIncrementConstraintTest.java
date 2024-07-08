package liquibase.statement;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AutoIncrementConstraintTest {
    
    @Test
    public void ctor() {
        AutoIncrementConstraint constraint = new AutoIncrementConstraint("COL_NAME");
        assertEquals("COL_NAME", constraint.getColumnName());
    }
}
