package liquibase.change;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import liquibase.database.statement.generator.*;
import liquibase.database.statement.SqlStatement;
import liquibase.database.statement.CreateTableStatement;
import liquibase.database.statement.AddAutoIncrementStatement;
import liquibase.database.statement.AddDefaultValueStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.Database;
import liquibase.database.MySQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.database.H2Database;

import java.util.List;
import java.util.SortedSet;

/**
 * Tests for {@link ChangeFactory}
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

        factory.unregister(change.getChangeName());
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
    @SuppressWarnings({"UnusedDeclaration"})
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
    @SuppressWarnings({"UnusedDeclaration"})
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
