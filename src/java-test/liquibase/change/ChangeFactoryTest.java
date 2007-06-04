package liquibase.change;

import junit.framework.TestCase;
import liquibase.migrator.parser.ChangeFactory;
import liquibase.migrator.change.AbstractChange;
import liquibase.migrator.change.CreateTableChange;

public class ChangeFactoryTest extends TestCase {

    public void testCreate() {
        ChangeFactory factory = new ChangeFactory();

        AbstractChange createTableChange = factory.create("createTable");
        assertEquals(CreateTableChange.class, createTableChange.getClass());


        try {
            factory.create("invalidChange");
        } catch (Exception e) {
            ; //that's what we wanted
        }
    }
}
