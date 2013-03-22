package liquibase.change;

import liquibase.change.core.RawSQLChange;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;

public class BaseSQLChangeTest extends StandardChangeTest {

    @Before
    public void setUp() throws Exception {
        super.testChangeInstance = new RawSQLChange();
    }

    @Override
    @Test
    public void getRefactoringName() throws Exception {
        assertEquals("sql", new RawSQLChange().getChangeMetaData().getName());
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
