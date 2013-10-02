package liquibase.change.core;

import liquibase.change.ChangeFactory;
import liquibase.change.StandardChangeTest;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link RawSQLChange}
 */
public abstract class RawSQLChangeTest extends StandardChangeTest {

    private RawSQLChange refactoring;

    @Before
    public void setUp() throws Exception {
        refactoring = new RawSQLChange();
    }

    @Override
    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("Custom SQL", ChangeFactory.getInstance().getChangeMetaData(new RawSQLChange()).getName());
    }

//    @Test
//    public void generateStatement() throws Exception {
//        refactoring.setSql("SQL STATEMENT HERE");
//        OracleDatabase database = new OracleDatabase();
//        assertEquals("SQL STATEMENT HERE", refactoring.generateStatements(database)[0].getSqlStatement(database));
//    }

    @Override
    @Test
    public void getConfirmationMessage() throws Exception {
        assertEquals("Custom SQL executed", refactoring.getConfirmationMessage());
    }
}