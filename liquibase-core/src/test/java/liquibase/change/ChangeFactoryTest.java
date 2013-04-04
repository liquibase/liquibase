package liquibase.change;

import liquibase.change.Change;
import liquibase.change.ChangeFactory;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import liquibase.change.ChangeMetaData;
import liquibase.change.core.AddAutoIncrementChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.DropTableChange;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.LiquibaseService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

public class ChangeFactoryTest {

    @Before
    public void setup() {
        ChangeFactory.reset();
        SometimesExceptionThrowingChange.timesCalled = 0;
    }

    @After
    public void resetRegistry() {
        ChangeFactory.reset();
    }

    @Test
    public void constructor() {
        ChangeFactory instance = ChangeFactory.getInstance();
        assertTrue(instance.getRegistry().containsKey("createTable"));
        assertTrue(instance.getRegistry().containsKey("dropTable"));
    }

    @Test
    public void getInstance() {
        assertNotNull(ChangeFactory.getInstance());

        assertTrue(ChangeFactory.getInstance() == ChangeFactory.getInstance());
    }

    @Test
    public void reset() {
        ChangeFactory instance1 = ChangeFactory.getInstance();
        ChangeFactory.reset();
        assertFalse(instance1 == ChangeFactory.getInstance());
    }

    @Test
    public void clear() {
        ChangeFactory changeFactory = ChangeFactory.getInstance();
        assertTrue(changeFactory.getRegistry().size() > 5);
        changeFactory.clear();
        assertEquals(0, changeFactory.getRegistry().size());
    }

    @Test
    public void register() {
        ChangeFactory changeFactory = ChangeFactory.getInstance();
        changeFactory.clear();

        assertEquals(0, changeFactory.getRegistry().size());
        changeFactory.register(CreateTableChange.class);

        assertEquals(1, changeFactory.getRegistry().size());
        assertTrue(changeFactory.getRegistry().containsKey("createTable"));

        changeFactory.register(Priority10Change.class);
        changeFactory.register(Priority5Change.class);
        changeFactory.register(AnotherPriority5Change.class); //only one should be stored

        assertEquals(3, changeFactory.getRegistry().get("createTable").size());
        assertEquals(Priority10Change.class, changeFactory.getRegistry().get("createTable").iterator().next());
    }

    @Test(expected = UnexpectedLiquibaseException.class)
    public void register_badClassRightAway() {
        ChangeFactory changeFactory = ChangeFactory.getInstance();

        changeFactory.register(ExceptionThrowingChange.class);
    }

    @Test(expected = UnexpectedLiquibaseException.class)
    public void register_badClassLaterInComparator() {
        ChangeFactory changeFactory = ChangeFactory.getInstance();

        changeFactory.register(SometimesExceptionThrowingChange.class);
        changeFactory.register(Priority5Change.class);
        changeFactory.register(Priority10Change.class);
    }

    @Test
    public void unregister_instance() {
        ChangeFactory factory = ChangeFactory.getInstance();

        factory.clear();

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

        factory.clear();

        assertEquals(0, factory.getRegistry().size());

        factory.register(CreateTableChange.class);
        factory.register(AddAutoIncrementChange.class);
        factory.register(DropTableChange.class);

        assertEquals(3, factory.getRegistry().size());

        factory.unregister("doesNoExist");
        assertEquals(3, factory.getRegistry().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getRegistry() {
        ChangeFactory.getInstance().getRegistry().put("x", new TreeSet<Class<? extends Change>>());
    }

    @Test
    public void create_exists() {
        Change change = ChangeFactory.getInstance().create("createTable");

        assertNotNull(change);
        assertTrue(change instanceof CreateTableChange);

        assertNotSame(change, ChangeFactory.getInstance().create("createTable"));
    }

    @Test
    public void create_notExists() {
        Change change = ChangeFactory.getInstance().create("badChangeName");

        assertNull(change);

    }

    @Test(expected = UnexpectedLiquibaseException.class)
    public void create_badClass() {
        ChangeFactory.getInstance().register(SometimesExceptionThrowingChange.class);
        Change change = ChangeFactory.getInstance().create("createTable");

        assertNotNull(change);
        assertTrue(change instanceof CreateTableChange);

    }

    @LiquibaseService(skip = true)
    public static class Priority5Change extends CreateTableChange {
        @Override
        public ChangeMetaData getChangeMetaData() {
            return new ChangeMetaData("createTable", null, 5, null, null, null);
        }
    }

    @LiquibaseService(skip = true)
    public static class Priority10Change extends CreateTableChange {
        @Override
        public ChangeMetaData getChangeMetaData() {
            return new ChangeMetaData("createTable", null, 10, null, null, null);
        }
    }

    @LiquibaseService(skip = true)
    public static class AnotherPriority5Change extends CreateTableChange {
        @Override
        public ChangeMetaData getChangeMetaData() {
            return new ChangeMetaData("createTable", null, 5, null, null, null);
        }
    }

    @LiquibaseService(skip = true)
    public static class ExceptionThrowingChange extends CreateTableChange {
        public ExceptionThrowingChange() {
            throw new RuntimeException("I throw exceptions");
        }

        @Override
        public ChangeMetaData getChangeMetaData() {
            return new ChangeMetaData("createTable", null, 15, null, null, null);
        }
    }

    @LiquibaseService(skip = true)
    public static class SometimesExceptionThrowingChange extends CreateTableChange {
        private static int timesCalled = 0;
        public SometimesExceptionThrowingChange() {
            if (timesCalled > 1) {
                throw new RuntimeException("I throw exceptions");
            }
            timesCalled++;
        }

        @Override
        public ChangeMetaData getChangeMetaData() {
            return new ChangeMetaData("createTable", null, 15, null, null, null);
        }
    }
}
