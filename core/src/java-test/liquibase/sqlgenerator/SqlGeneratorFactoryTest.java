package liquibase.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.statement.AddAutoIncrementStatement;
import liquibase.statement.SqlStatement;
import liquibase.sqlgenerator.core.AddColumnGenerator;
import liquibase.sqlgenerator.core.AddAutoIncrementGenerator;
import liquibase.sqlgenerator.core.AddAutoIncrementGeneratorHsql;
import liquibase.sqlgenerator.core.AddAutoIncrementGeneratorDB2;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.SortedSet;
import java.util.Collection;

public class SqlGeneratorFactoryTest {

    @Before
    public void setup() {
        SqlGeneratorFactory.reset();

    }

    @Test
    public void getInstance() {
        assertNotNull(SqlGeneratorFactory.getInstance());
        
        assertTrue(SqlGeneratorFactory.getInstance() == SqlGeneratorFactory.getInstance());
    }

    @Test
    public void register() {
        SqlGeneratorFactory.getInstance().getGenerators().clear();

        assertEquals(0, SqlGeneratorFactory.getInstance().getGenerators().size());

        SqlGeneratorFactory.getInstance().register(new MockSqlGenerator(1, "A1"));

        assertEquals(1, SqlGeneratorFactory.getInstance().getGenerators().size());
    }

    @Test
    public void unregister_instance() {
        SqlGeneratorFactory factory = SqlGeneratorFactory.getInstance();

        factory.getGenerators().clear();

        assertEquals(0, factory.getGenerators().size());

        AddAutoIncrementGeneratorHsql sqlGenerator 
        	= new AddAutoIncrementGeneratorHsql();

        factory.register(new AddAutoIncrementGenerator());
        factory.register(sqlGenerator);
        factory.register(new AddAutoIncrementGeneratorDB2());

        assertEquals(3, factory.getGenerators().size());

        factory.unregister(sqlGenerator);
        assertEquals(2, factory.getGenerators().size());
    }

    @Test
    public void unregister_class() {
        SqlGeneratorFactory factory = SqlGeneratorFactory.getInstance();

        factory.getGenerators().clear();

        assertEquals(0, factory.getGenerators().size());

        AddAutoIncrementGeneratorHsql sqlGenerator 
        			= new AddAutoIncrementGeneratorHsql();

        factory.register(new AddAutoIncrementGenerator());
        factory.register(sqlGenerator);
        factory.register(new AddAutoIncrementGeneratorDB2());

        assertEquals(3, factory.getGenerators().size());

        factory.unregister(AddAutoIncrementGeneratorHsql.class);
        assertEquals(2, factory.getGenerators().size());
    }

     @Test
    public void unregister_class_doesNotExist() {
        SqlGeneratorFactory factory = SqlGeneratorFactory.getInstance();

        factory.getGenerators().clear();

        assertEquals(0, factory.getGenerators().size());

        factory.register(new AddAutoIncrementGenerator());
        factory.register(new AddAutoIncrementGeneratorHsql());
        factory.register(new AddAutoIncrementGeneratorDB2());

        assertEquals(3, factory.getGenerators().size());

        factory.unregister(AddColumnGenerator.class);
        assertEquals(3, factory.getGenerators().size());
    }

   @Test
    public void reset() {
        SqlGeneratorFactory instance1 = SqlGeneratorFactory.getInstance();
        SqlGeneratorFactory.reset();
        assertFalse(instance1 == SqlGeneratorFactory.getInstance());
    }

    @SuppressWarnings("unchecked")
	@Test
    public void builtInGeneratorsAreFound() {
        Collection<SqlGenerator> generators = SqlGeneratorFactory.getInstance().getGenerators();
        assertTrue(generators.size() > 10);
    }

    @Test
    public void getGenerators() {
        SortedSet<SqlGenerator> allGenerators = SqlGeneratorFactory.getInstance().getGenerators(new AddAutoIncrementStatement(null, "person", "name", "varchar(255)"), new H2Database());

        assertNotNull(allGenerators);
        assertEquals(1, allGenerators.size());        
    }

    private SqlGenerator addGenerator(final Class<? extends SqlStatement> sqlStatementClass, final Class<? extends Database> sqlDatabaseClass, final int level) {
    	
        SqlGenerator generator = new SqlGenerator() {
            public int getPriority() {
                return level;
            }

            public ValidationErrors validate(SqlStatement sqlStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
                return new ValidationErrors();
            }

            public boolean supports(SqlStatement statement, Database database) {
            	boolean ret = sqlStatementClass.isAssignableFrom(statement.getClass()) && sqlDatabaseClass.isAssignableFrom(database.getClass()); 
                return ret;
            }

            public Sql[] generateSql(SqlStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
                return new Sql[0];
            }
        };
        SqlGeneratorFactory.getInstance().register(generator);

        return generator;
    }
}
