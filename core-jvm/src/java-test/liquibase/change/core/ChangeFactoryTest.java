package liquibase.change.core;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import liquibase.change.core.AddAutoIncrementChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.DropTableChange;
import liquibase.change.ChangeFactory;
import liquibase.change.Change;

/**
 * Tests for {@link liquibase.change.ChangeFactory}
 */
public class ChangeFactoryTest {

    @Before
    public void setup() {
        ChangeFactory.reset();

    }

    @Test
    public void getInstance() {
        assertNotNull(ChangeFactory.getInstance());

        assertTrue(ChangeFactory.getInstance() == ChangeFactory.getInstance());
    }

    @Test
    public void register() {
        ChangeFactory.getInstance().getRegistry().clear();

        assertEquals(0, ChangeFactory.getInstance().getRegistry().size());

        ChangeFactory.getInstance().register(CreateTableChange.class);

        assertEquals(1, ChangeFactory.getInstance().getRegistry().size());
    }

    @Test
    public void unregister_instance() {
        ChangeFactory factory = ChangeFactory.getInstance();

        factory.getRegistry().clear();

        assertEquals(0, factory.getRegistry().size());

        AddAutoIncrementChange change = new AddAutoIncrementChange();

        factory.register(CreateTableChange.class);
        factory.register(change.getClass());
        factory.register(DropTableChange.class);

        assertEquals(3, factory.getRegistry().size());

        factory.unregister(change.getChangeMetaData().getName());
        assertEquals(2, factory.getRegistry().size());
    }

    @Test
    public void unregister_doesNotExist() {
        ChangeFactory factory = ChangeFactory.getInstance();

        factory.getRegistry().clear();

        assertEquals(0, factory.getRegistry().size());

        factory.register(CreateTableChange.class);
        factory.register(AddAutoIncrementChange.class);
        factory.register(DropTableChange.class);

        assertEquals(3, factory.getRegistry().size());

        factory.unregister("doesNoExist");
        assertEquals(3, factory.getRegistry().size());
    }

    @Test
    public void create_exists() {
        Change change = ChangeFactory.getInstance().create("createTable");

        assertNotNull(change);
        assertTrue(change instanceof CreateTableChange);

    }

    @Test
    public void builtInGeneratorsAreFound() {
        assertTrue(ChangeFactory.getInstance().getRegistry().size() > 10);
    }

    @Test
    public void create_notExists() {
        Change change = ChangeFactory.getInstance().create("badChangeName");

        assertNull(change);

    }

    @Test
    public void reset() {
        ChangeFactory instance1 = ChangeFactory.getInstance();
        ChangeFactory.reset();
        assertFalse(instance1 == ChangeFactory.getInstance());
    }
}
