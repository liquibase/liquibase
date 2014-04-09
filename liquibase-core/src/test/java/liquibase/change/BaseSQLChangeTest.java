package liquibase.change;

import liquibase.change.core.RawSQLChange;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class BaseSQLChangeTest extends StandardChangeTest {

    @Before
    public void setUp() throws Exception {
        super.testChangeInstance = new RawSQLChange();
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
