package liquibase.statement;

import static org.junit.Assert.*;
import org.junit.Test;

public class AutoIncrementConstraintTest {
    
    @Test
    public void ctor() {
        AutoIncrementConstraint constraint = new AutoIncrementConstraint("COL_NAME");
        assertEquals("COL_NAME", constraint.getColumnName());
    }
}
