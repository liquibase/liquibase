package liquibase.change;

import liquibase.database.*;
import liquibase.statement.AlterSequenceStatement;
import liquibase.statement.SqlStatement;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Tests for {@link AlterSequenceChange}
 */
public class AlterSequenceChangeTest extends AbstractChangeTest {

    @Override
    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Alter Sequence", new AlterSequenceChange().getChangeMetaData().getDescription());
    }

    @Override
    @Test
    public void generateStatement() throws Exception {
        AlterSequenceChange refactoring = new AlterSequenceChange();
        refactoring.setSchemaName("SCHEMA_NAME");
        refactoring.setSequenceName("SEQ_NAME");
        refactoring.setMinValue(100);
        refactoring.setMaxValue(1000);
        refactoring.setIncrementBy(50);
        refactoring.setOrdered(true);

        SqlStatement[] sqlStatements = refactoring.generateStatements(new MockDatabase());

        assertEquals(1, sqlStatements.length);
        assertTrue(sqlStatements[0] instanceof AlterSequenceStatement);
        assertEquals("SCHEMA_NAME", ((AlterSequenceStatement) sqlStatements[0]).getSchemaName());
        assertEquals("SEQ_NAME", ((AlterSequenceStatement) sqlStatements[0]).getSequenceName());
        assertEquals(new Integer(100), ((AlterSequenceStatement) sqlStatements[0]).getMinValue());
        assertEquals(new Integer(1000), ((AlterSequenceStatement) sqlStatements[0]).getMaxValue());
        assertEquals(new Integer(50), ((AlterSequenceStatement) sqlStatements[0]).getIncrementBy());
        assertEquals(true, ((AlterSequenceStatement) sqlStatements[0]).getOrdered());

    }

    @Override
    @Test
    public void getConfirmationMessage() throws Exception {
        AlterSequenceChange refactoring = new AlterSequenceChange();
        refactoring.setSequenceName("SEQ_NAME");

        assertEquals("Sequence SEQ_NAME altered", refactoring.getConfirmationMessage());
    }

    @Override
    protected boolean changeIsUnsupported(Database database) {
        return database instanceof SQLiteDatabase
                || database instanceof SybaseASADatabase
                || database instanceof MSSQLDatabase
                || database instanceof MySQLDatabase
                || database instanceof DerbyDatabase
                || database instanceof SybaseDatabase
                || database instanceof CacheDatabase;
    }
}
