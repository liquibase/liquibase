package liquibase.sqlgenerator;

import liquibase.database.core.H2Database;
import liquibase.sqlgenerator.core.AddAutoIncrementGenerator;
import liquibase.sqlgenerator.core.AddAutoIncrementGeneratorDB2;
import liquibase.sqlgenerator.core.AddAutoIncrementGeneratorHsqlH2;
import liquibase.sqlgenerator.core.AddColumnGenerator;
import liquibase.statement.core.AddAutoIncrementStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.SortedSet;

import static org.junit.Assert.*;

public class SqlGeneratorFactoryTest {

    private AddAutoIncrementStatement statement;
    private H2Database database;
    private SqlGeneratorFactory factory;

    @Before
    public void setUp() {
        statement = new AddAutoIncrementStatement(null, null, "person", "name", "varchar(255)", null, null, null, null);
        database = new H2Database();
        factory = SqlGeneratorFactory.getInstance();
    }

    @After
    public void teardown() {
        SqlGeneratorFactory.reset();
    }

    @Test
    public void getInstance() {
        assertNotNull(SqlGeneratorFactory.getInstance());
        
        assertTrue(SqlGeneratorFactory.getInstance() == SqlGeneratorFactory.getInstance());
    }

    @Test
    public void register() {
        factory.getGenerators().clear();

        assertEquals(0, factory.getGenerators().size());

        factory.register(new MockSqlGenerator(1, "A1"));

        assertEquals(1, factory.getGenerators().size());
    }

    @Test
    public void unregisterInstance() {
        factory.getGenerators().clear();

        assertEquals(0, factory.getGenerators().size());

        AddAutoIncrementGeneratorHsqlH2 sqlGenerator = new AddAutoIncrementGeneratorHsqlH2();

        factory.register(new AddAutoIncrementGenerator());
        factory.register(sqlGenerator);
        factory.register(new AddAutoIncrementGeneratorDB2());

        assertEquals(3, factory.getGenerators().size());

        factory.unregister(sqlGenerator);
        assertEquals(2, factory.getGenerators().size());
    }

    @Test
    public void unregisterClass() {
        factory.getGenerators().clear();

        assertEquals(0, factory.getGenerators().size());

        AddAutoIncrementGeneratorHsqlH2 sqlGenerator = new AddAutoIncrementGeneratorHsqlH2();

        factory.register(new AddAutoIncrementGenerator());
        factory.register(sqlGenerator);
        factory.register(new AddAutoIncrementGeneratorDB2());

        assertEquals(3, factory.getGenerators().size());

        factory.unregister(AddAutoIncrementGeneratorHsqlH2.class);
        assertEquals(2, factory.getGenerators().size());
    }

     @Test
    public void unregisterClassDoesNotExist() {
        factory.getGenerators().clear();

        assertEquals(0, factory.getGenerators().size());

        factory.register(new AddAutoIncrementGenerator());
        factory.register(new AddAutoIncrementGeneratorHsqlH2());
        factory.register(new AddAutoIncrementGeneratorDB2());

        assertEquals(3, factory.getGenerators().size());

        factory.unregister(AddColumnGenerator.class);
        assertEquals(3, factory.getGenerators().size());
    }

    @Test
    public void registerWithCache() {
        factory.getGenerators().clear();

        assertEquals(0, factory.getGenerators(statement, database).size());

        factory.register(new AddAutoIncrementGeneratorHsqlH2());

        assertEquals(1, factory.getGenerators(statement, database).size());
    }

    @Test
    public void unregisterInstanceWithCache() {
        factory.getGenerators().clear();

        assertEquals(0, factory.getGenerators(statement, database).size());

        AddAutoIncrementGeneratorHsqlH2 sqlGenerator = new AddAutoIncrementGeneratorHsqlH2();

        factory.register(new CustomAddAutoIncrementGeneratorHsqlH2());
        factory.register(sqlGenerator);

        assertEquals(2, factory.getGenerators(statement, database).size());

        factory.unregister(sqlGenerator);
        assertEquals(1, factory.getGenerators(statement, database).size());
    }

    @Test
    public void unregisterClassWithCache() {
        factory.getGenerators().clear();

        assertEquals(0, factory.getGenerators(statement, database).size());

        CustomAddAutoIncrementGeneratorHsqlH2 sqlGenerator = new CustomAddAutoIncrementGeneratorHsqlH2();
        factory.register(sqlGenerator);
        factory.register(new AddAutoIncrementGeneratorHsqlH2());

        assertEquals(2, factory.getGenerators(statement, database).size());

        factory.unregister(AddAutoIncrementGeneratorHsqlH2.class);
        assertEquals(1, factory.getGenerators(statement, database).size());
    }

    @Test
    public void unregisterClassDoesNotExistWithCache() {
        factory.getGenerators().clear();

        assertEquals(0, factory.getGenerators(statement, database).size());

        factory.register(new CustomAddAutoIncrementGeneratorHsqlH2());
        factory.register(new AddAutoIncrementGeneratorHsqlH2());

        assertEquals(2, factory.getGenerators(statement, database).size());

        factory.unregister(AddColumnGenerator.class);
        assertEquals(2, factory.getGenerators(statement, database).size());
    }


    @Test
    public void reset() {
        SqlGeneratorFactory.reset();
        assertFalse(factory == SqlGeneratorFactory.getInstance());
    }

    @SuppressWarnings("unchecked")
	@Test
    public void builtInGeneratorsAreFound() {
        Collection<SqlGenerator> generators = factory.getGenerators();
        assertTrue(generators.size() > 10);
    }

    @Test
    public void getGenerators() {
        SortedSet<SqlGenerator> allGenerators = SqlGeneratorFactory.getInstance().getGenerators(new AddAutoIncrementStatement(null, null, "person", "name", "varchar(255)", null, null, null, null), new H2Database());

        assertNotNull(allGenerators);
        assertEquals(1, allGenerators.size());        
    }

    private class CustomAddAutoIncrementGeneratorHsqlH2 extends AddAutoIncrementGeneratorHsqlH2 {

        @Override
        public int getPriority() {
            return super.getPriority() + 1;
        }
    }
}
