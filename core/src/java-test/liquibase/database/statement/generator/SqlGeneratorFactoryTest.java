package liquibase.database.statement.generator;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import liquibase.database.statement.AddAutoIncrementStatement;
import liquibase.database.statement.SqlStatement;
import liquibase.database.statement.CreateTableStatement;
import liquibase.database.statement.AddDefaultValueStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.MySQLDatabase;
import liquibase.database.Database;
import liquibase.database.OracleDatabase;
import liquibase.database.HsqlDatabase;

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

        SqlGeneratorFactory.getInstance().register(new SqlGenerator() {
            public int getSpecializationLevel() {
                return 0;
            }

            public boolean isValid(SqlStatement statement, Database database) {
                return false;
            }

            public Sql[] generateSql(SqlStatement statement, Database database) {
                return new Sql[0];
            }
        });

        assertEquals(1, SqlGeneratorFactory.getInstance().getGenerators().size());
    }

    @Test
    @SuppressWarnings({"UnusedDeclaration"})
    public void getBestGenerator_hasBest() {
        SqlGeneratorFactory.getInstance().getGenerators().clear();

        SqlGenerator mysqlIncorrectGenerator = addGenerator(CreateTableStatement.class, MySQLDatabase.class, 1);
        SqlGenerator oracleIncorrectGenerator = addGenerator(CreateTableStatement.class, OracleDatabase.class, 1);
        SqlGenerator mysqlBetterGenerator = addGenerator(AddAutoIncrementStatement.class, MySQLDatabase.class, 2);
        SqlGenerator mysqlCorrectGenerator = addGenerator(AddAutoIncrementStatement.class, MySQLDatabase.class, 1);
        SqlGenerator oracleCorrectGenerator = addGenerator(AddAutoIncrementStatement.class, OracleDatabase.class, 1);

        SqlGenerator bestSqlGenerator = SqlGeneratorFactory.getInstance().getBestGenerator(new AddAutoIncrementStatement(null, "person", "name", "varchar(255)"), new MySQLDatabase());

        assertNotNull(bestSqlGenerator);
        assertTrue(mysqlBetterGenerator == bestSqlGenerator);

    }

    @Test
    @SuppressWarnings({"UnusedDeclaration"})
    public void getBestGenerator_noneMatching() {
        SqlGeneratorFactory.getInstance().getGenerators().clear();

        SqlGenerator mysqlIncorrectGenerator = addGenerator(CreateTableStatement.class, MySQLDatabase.class, 1);
        SqlGenerator oracleIncorrectGenerator = addGenerator(CreateTableStatement.class, OracleDatabase.class, 1);
        SqlGenerator mysqlBetterGenerator = addGenerator(AddAutoIncrementStatement.class, MySQLDatabase.class, 2);
        SqlGenerator mysqlCorrectGenerator = addGenerator(AddAutoIncrementStatement.class, MySQLDatabase.class, 1);
        SqlGenerator oracleCorrectGenerator = addGenerator(AddAutoIncrementStatement.class, OracleDatabase.class, 1);

        SqlGenerator bestSqlGenerator = SqlGeneratorFactory.getInstance().getBestGenerator(new AddDefaultValueStatement(null, "person", "name", "N/A"), new MySQLDatabase());

        assertNotNull(bestSqlGenerator);
        assertTrue(bestSqlGenerator instanceof NotImplementedGenerator);
    }

    @Test
    public void reset() {
        SqlGeneratorFactory instance1 = SqlGeneratorFactory.getInstance();
        SqlGeneratorFactory.reset();
        assertFalse(instance1 == SqlGeneratorFactory.getInstance());
    }

    @Test
    public void builtInGeneratorsAreFound() {
        List<SqlGenerator> generators = SqlGeneratorFactory.getInstance().getGenerators();
        assertTrue(generators.size() > 0);        
    }

    @SuppressWarnings({"UnusedDeclaration"})
    @Test
    public void getAllGenerators() {
        SortedSet<SqlGenerator> allGenerators = SqlGeneratorFactory.getInstance().getAllGenerators(new AddAutoIncrementStatement(null, "person", "name", "varchar(255)"), new HsqlDatabase());

        assertNotNull(allGenerators);
        assertEquals(2, allGenerators.size());        
    }

    private SqlGenerator addGenerator(final Class<? extends SqlStatement> createTableStatementClass, final Class<? extends Database> sqlDatabaseClass, final int level) {
        SqlGenerator generator = new SqlGenerator() {
            public int getSpecializationLevel() {
                return level;
            }

            public boolean isValid(SqlStatement statement, Database database) {
                return createTableStatementClass.isAssignableFrom(statement.getClass()) && sqlDatabaseClass.isAssignableFrom(database.getClass());
            }

            public Sql[] generateSql(SqlStatement statement, Database database) {
                return new Sql[0];
            }
        };
        SqlGeneratorFactory.getInstance().register(generator);

        return generator;
    }
}
