package liquibase.database.statement;

import static org.junit.Assert.*;
import org.junit.Test;
import liquibase.database.statement.AutoIncrementConstraint;

public class AutoIncrementConstraintTest {
    
    @Test
    public void ctor() {
        AutoIncrementConstraint constraint = new AutoIncrementConstraint("COL_NAME");
        assertEquals("COL_NAME", constraint.getColumnName());
    }
}
