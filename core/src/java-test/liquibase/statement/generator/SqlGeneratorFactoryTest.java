package liquibase.statement.generator;

import liquibase.database.Database;
import liquibase.database.H2Database;
import liquibase.database.MySQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.statement.AddAutoIncrementStatement;
import liquibase.statement.AddDefaultValueStatement;
import liquibase.statement.CreateTableStatement;
import liquibase.statement.SqlStatement;
import liquibase.statement.syntax.Sql;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.SortedSet;

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

        SqlGeneratorFactory.getInstance().register(new MockSqlGenerator<SqlStatement>());

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
    @SuppressWarnings({"unused"})
    public void getBestGenerator_hasBest() {
        SqlGeneratorFactory.getInstance().getGenerators().clear();

        SqlGenerator<CreateTableStatement> mysqlIncorrectGenerator = addGenerator(CreateTableStatement.class, MySQLDatabase.class, 1);
        SqlGenerator<CreateTableStatement> oracleIncorrectGenerator = addGenerator(CreateTableStatement.class, OracleDatabase.class, 1);
        SqlGenerator<AddAutoIncrementStatement> mysqlBetterGenerator = addGenerator(AddAutoIncrementStatement.class, MySQLDatabase.class, 2);
        SqlGenerator<AddAutoIncrementStatement> mysqlCorrectGenerator = addGenerator(AddAutoIncrementStatement.class, MySQLDatabase.class, 1);
        SqlGenerator<AddAutoIncrementStatement> oracleCorrectGenerator = addGenerator(AddAutoIncrementStatement.class, OracleDatabase.class, 1);

        SqlGenerator<AddAutoIncrementStatement> bestSqlGenerator = SqlGeneratorFactory.getInstance().getBestGenerator(new AddAutoIncrementStatement(null, "person", "name", "varchar(255)"), new MySQLDatabase());

        assertNotNull(bestSqlGenerator);
        assertTrue(mysqlBetterGenerator == bestSqlGenerator);

    }

    @Test
    @SuppressWarnings({"unused"})
    public void getBestGenerator_noneMatching() {
        SqlGeneratorFactory.getInstance().getGenerators().clear();

        SqlGenerator<CreateTableStatement> mysqlIncorrectGenerator = addGenerator(CreateTableStatement.class, MySQLDatabase.class, 1);
        SqlGenerator<CreateTableStatement> oracleIncorrectGenerator = addGenerator(CreateTableStatement.class, OracleDatabase.class, 1);
        SqlGenerator<AddAutoIncrementStatement> mysqlBetterGenerator = addGenerator(AddAutoIncrementStatement.class, MySQLDatabase.class, 2);
        SqlGenerator<AddAutoIncrementStatement> mysqlCorrectGenerator = addGenerator(AddAutoIncrementStatement.class, MySQLDatabase.class, 1);
        SqlGenerator<AddAutoIncrementStatement> oracleCorrectGenerator = addGenerator(AddAutoIncrementStatement.class, OracleDatabase.class, 1);

        SqlGenerator<AddDefaultValueStatement> bestSqlGenerator = SqlGeneratorFactory.getInstance().getBestGenerator(new AddDefaultValueStatement(null, "person", "name", "N/A"), new MySQLDatabase());

        assertNull(bestSqlGenerator);
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
        List<SqlGenerator> generators = SqlGeneratorFactory.getInstance().getGenerators();
        assertTrue(generators.size() > 0);        
    }

    @Test
    public void getAllGenerators() {
        SortedSet<SqlGenerator> allGenerators = SqlGeneratorFactory.getInstance().getAllGenerators(new AddAutoIncrementStatement(null, "person", "name", "varchar(255)"), new H2Database());

        assertNotNull(allGenerators);
        assertEquals(1, allGenerators.size());        
    }

    private SqlGenerator addGenerator(final Class<? extends SqlStatement> sqlStatementClass, final Class<? extends Database> sqlDatabaseClass, final int level) {
    	
        SqlGenerator generator = new SqlGenerator() {
            public int getSpecializationLevel() {
                return level;
            }

            public GeneratorValidationErrors validate(SqlStatement sqlStatement, Database database) {
                return new GeneratorValidationErrors();
            }

            public boolean isValidGenerator(SqlStatement statement, Database database) {
            	boolean ret = sqlStatementClass.isAssignableFrom(statement.getClass()) && sqlDatabaseClass.isAssignableFrom(database.getClass()); 
                return ret;
            }

            public Sql[] generateSql(SqlStatement statement, Database database) {
                return new Sql[0];
            }
        };
        SqlGeneratorFactory.getInstance().register(generator);

        return generator;
    }

    private static class MockSqlGenerator<T extends SqlStatement> implements SqlGenerator<T> {
        public int getSpecializationLevel() {
            return 0;
        }

        public boolean isValidGenerator(SqlStatement statement, Database database) {
            return false;
        }

        public GeneratorValidationErrors validate(SqlStatement sqlStatement, Database database) {
            return new GeneratorValidationErrors();
        }

        public Sql[] generateSql(SqlStatement statement, Database database) {
            return new Sql[0];
        }
    }
}
