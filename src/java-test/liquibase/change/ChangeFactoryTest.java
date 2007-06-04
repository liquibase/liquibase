package liquibase.change;

import junit.framework.TestCase;
import liquibase.migrator.change.ChangeFactory;
import liquibase.migrator.change.AbstractChange;
import liquibase.migrator.change.CreateTableChange;

public class ChangeFactoryTest extends TestCase {
    public void testGetInstance() {
        ChangeFactory firstCall = ChangeFactory.getInstance();
        ChangeFactory secondCall = ChangeFactory.getInstance();

        assertSame("First and second calls to getInstance returned different instances.  Singleton not implemented correctly",
                firstCall, secondCall);
    }

    public void testCreate() {
        ChangeFactory factory = ChangeFactory.getInstance();

        AbstractChange createTableChange = factory.create("createTable");
        assertEquals(CreateTableChange.class, createTableChange.getClass());


        try {
            factory.create("invalidChange");
        } catch (Exception e) {
            ; //that's what we wanted
        }
    }
}
