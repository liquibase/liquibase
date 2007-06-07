package liquibase.change;

import junit.framework.TestCase;
import liquibase.migrator.change.Change;
import liquibase.migrator.change.CreateTableChange;
import liquibase.migrator.parser.ChangeFactory;

public class ChangeFactoryTest extends TestCase {

    public void testCreate() {
        ChangeFactory factory = new ChangeFactory();

        Change createTableChange = factory.create("createTable");
        assertEquals(CreateTableChange.class, createTableChange.getClass());


        try {
            factory.create("invalidChange");
        } catch (Exception e) {
            ; //that's what we wanted
        }
    }
}
