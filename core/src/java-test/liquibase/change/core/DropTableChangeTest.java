package liquibase.change.core;

import liquibase.database.core.MockDatabase;
import liquibase.statement.DropTableStatement;
import liquibase.statement.SqlStatement;
import liquibase.change.core.DropTableChange;
import liquibase.change.AbstractChangeTest;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link DropTableChange}
 */
public class DropTableChangeTest extends AbstractChangeTest {
    private DropTableChange change;

    @Before
    public void setUp() throws Exception {
        change = new DropTableChange();
        change.setSchemaName("SCHEMA_NAME");
        change.setTableName("TAB_NAME");
        change.setCascadeConstraints(true);
    }

    @Override
    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Drop Table", change.getChangeMetaData().getDescription());
    }

    @Override
    @Test
    public void generateStatement() throws Exception {
        SqlStatement[] sqlStatements = change.generateStatements(new MockDatabase());
        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof DropTableStatement);
        assertEquals("SCHEMA_NAME", ((DropTableStatement) sqlStatements[0]).getSchemaName());
        assertEquals("TAB_NAME", ((DropTableStatement) sqlStatements[0]).getTableName());
        assertTrue(((DropTableStatement) sqlStatements[0]).isCascadeConstraints());
    }

    @Test
    public void generateStatement_nullCascadeConstraints() throws Exception {
        change.setCascadeConstraints(null);
        SqlStatement[] sqlStatements = change.generateStatements(new MockDatabase());
        assertFalse(((DropTableStatement) sqlStatements[0]).isCascadeConstraints());
    }

    @Override
    @Test
    public void getConfirmationMessage() throws Exception {
        assertEquals("Table TAB_NAME dropped", change.getConfirmationMessage());
    }
}