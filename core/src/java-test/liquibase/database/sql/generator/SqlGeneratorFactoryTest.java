package liquibase.database.sql.generator;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import liquibase.database.sql.AddAutoIncrementStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.sql.CreateTableStatement;
import liquibase.database.sql.AddDefaultValueStatement;
import liquibase.database.MySQLDatabase;
import liquibase.database.Database;
import liquibase.database.OracleDatabase;

public class SqlGeneratorFactoryTest {

    @Before
    public void setup() {
        SqlGeneratorFactory.getInstance().getGenerators().clear();
    }

    @Test
    public void getInstance() {
        assertNotNull(SqlGeneratorFactory.getInstance());
        
        assertTrue(SqlGeneratorFactory.getInstance() == SqlGeneratorFactory.getInstance());
    }

    @Test
    public void register() {
        assertEquals(0, SqlGeneratorFactory.getInstance().getGenerators().size());

        SqlGeneratorFactory.getInstance().register(new SqlGenerator() {
            public int getApplicability(SqlStatement statement, Database database) {
                return 0;
            }

            public String[] generateSql(SqlStatement statement, Database database) {
                return new String[0];
            }
        });

        assertEquals(1, SqlGeneratorFactory.getInstance().getGenerators().size());
    }

    @Test
    @SuppressWarnings({"UnusedDeclaration"})
    public void getBestGenerator_hasBest() {
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

        SqlGenerator mysqlIncorrectGenerator = addGenerator(CreateTableStatement.class, MySQLDatabase.class, 1);
        SqlGenerator oracleIncorrectGenerator = addGenerator(CreateTableStatement.class, OracleDatabase.class, 1);
        SqlGenerator mysqlBetterGenerator = addGenerator(AddAutoIncrementStatement.class, MySQLDatabase.class, 2);
        SqlGenerator mysqlCorrectGenerator = addGenerator(AddAutoIncrementStatement.class, MySQLDatabase.class, 1);
        SqlGenerator oracleCorrectGenerator = addGenerator(AddAutoIncrementStatement.class, OracleDatabase.class, 1);

        SqlGenerator bestSqlGenerator = SqlGeneratorFactory.getInstance().getBestGenerator(new AddDefaultValueStatement(null, "person", "name", "N/A"), new MySQLDatabase());

        assertNotNull(bestSqlGenerator);
        assertTrue(bestSqlGenerator instanceof NotImplementedGenerator);
    }

    private SqlGenerator addGenerator(final Class<? extends SqlStatement> createTableStatementClass, final Class<? extends Database> sqlDatabaseClass, final int applicability) {
        SqlGenerator generator = new SqlGenerator() {
            public int getApplicability(SqlStatement statement, Database database) {
                if (createTableStatementClass.isAssignableFrom(statement.getClass()) && sqlDatabaseClass.isAssignableFrom(database.getClass())) {
                    return applicability;
                } else {
                    return -1;
                }
            }

            public String[] generateSql(SqlStatement statement, Database database) {
                return new String[0];
            }
        };
        SqlGeneratorFactory.getInstance().register(generator);

        return generator;
    }
}
