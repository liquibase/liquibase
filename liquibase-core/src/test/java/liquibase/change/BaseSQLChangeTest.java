package liquibase.change;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import liquibase.change.core.RawSQLChange;

public class BaseSQLChangeTest extends StandardChangeTest {

    @Before
    public void setUp() throws Exception {
        super.testChangeInstance = new RawSQLChange();
    }

    @Override
    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("sql", ChangeFactory.getInstance().getChangeMetaData(new RawSQLChange()).getName());
    }

    @Override
    public void generateStatement() throws Exception {
    }

    @Override
    @Test
    public void getConfirmationMessage() throws Exception {
        assertEquals("Custom SQL executed", testChangeInstance.getConfirmationMessage());
    }


}
