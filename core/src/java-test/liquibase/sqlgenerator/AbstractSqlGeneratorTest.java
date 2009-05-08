package liquibase.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.template.Executor;
import liquibase.exception.JDBCException;
import liquibase.sql.Sql;
import liquibase.statement.CreateTableStatement;
import liquibase.statement.SqlStatement;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractSqlGeneratorTest<T extends SqlStatement> {

    protected SqlGenerator<T> generatorUnderTest;
    private List<? extends SqlStatement> setupStatements;

    public AbstractSqlGeneratorTest(SqlGenerator generatorUnderTest) throws Exception {
        this.generatorUnderTest = generatorUnderTest;
        for (Database database : TestContext.getInstance().getAvailableDatabases()) {
        	this.setupStatements = setupStatements(database);
        }
    }

    protected abstract T createSampleSqlStatement();

    protected abstract List<? extends SqlStatement>  setupStatements(Database database);

    protected void dropAndCreateTable(CreateTableStatement statement, Database database) throws SQLException, JDBCException {
        new Executor(database).execute(statement);

        if (!database.getAutoCommitMode()) {
            database.getConnection().commit();
        }

    }

    public void resetAvailableDatabases() throws Exception {
        for (Database database : TestContext.getInstance().getAvailableDatabases()) {
            if (database.supportsSchemas()) {
                database.dropDatabaseObjects(TestContext.ALT_SCHEMA);
            }
            database.dropDatabaseObjects(null);

            if (setupStatements != null) {
	            for (SqlStatement statement : setupStatements) {
	                new Executor(database).execute(statement);
	            }
            }
        }
    }


    @Test
    public void isImplementation() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            boolean isImpl = generatorUnderTest.isValidGenerator(createSampleSqlStatement(), database);
            if (shouldBeImplementation(database)) {
                assertTrue("Unexpected false isValidGenerator for " + database.getProductName(), isImpl);
            } else {
                assertFalse("Unexpected true isValidGenerator for " + database.getProductName(), isImpl);
            }
        }
    }

    @Test
    public void isValid() throws Exception {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
        	if (shouldBeImplementation(database)) {
        		assertFalse("isValid failed against " + database, generatorUnderTest.validate(createSampleSqlStatement(), database).hasErrors());
        	} else if (waitForException(database)) {
        		assertTrue("The validation should be failed for " + database, generatorUnderTest.validate(createSampleSqlStatement(), database).hasErrors());
        	}
        }
    }

    @Test
    public void checkExpectedGenerator() {
        assertEquals(this.getClass().getName().replaceFirst("Test$", ""), generatorUnderTest.getClass().getName());
    }

    protected boolean waitForException(Database database) {
        return false;
    }

    protected boolean shouldBeImplementation(Database database) {
        return true;
    }

    protected void testSqlOnAll(String expectedSql, T sqlStatement) throws Exception {
        testSql(expectedSql, sqlStatement, null, null);        
    }

    protected void testSqlOn(String expectedSql, T sqlStatement, Class<? extends Database>... includeDatabases) throws Exception {
        testSql(expectedSql, sqlStatement, includeDatabases, null);
    }

    public void testSqlOnAllExcept(String expectedSql, T sqlStatement, Class<? extends Database>... excludedDatabases) throws Exception {
        testSql(expectedSql,  sqlStatement, null, excludedDatabases);
    }

    private void testSql(String expectedSql, T sqlStatement, Class<? extends Database>[] includeDatabases, Class<? extends Database>[] excludeDatabases) throws Exception {

        if (expectedSql != null) {
            for (Database database : TestContext.getInstance().getAllDatabases()) {
                if (shouldTestDatabase(sqlStatement, database, includeDatabases, excludeDatabases)) {
                    String convertedSql =  replaceEscaping(expectedSql, database);
                    convertedSql = replaceDatabaseClauses(convertedSql, database);
                    convertedSql = replaceStandardTypes(convertedSql, database);

                    Sql[] sql = generatorUnderTest.generateSql(sqlStatement, database);

                    assertEquals(1, sql.length);
                    assertEquals("Incorrect SQL for " + database, convertedSql.toLowerCase(), sql[0].toSql().toLowerCase());
                }
            }
        }

        resetAvailableDatabases();
        for (Database availableDatabase : TestContext.getInstance().getAvailableDatabases()) {
            Statement statement = availableDatabase.getConnection().createStatement();
            if (shouldTestDatabase(sqlStatement, availableDatabase, includeDatabases, excludeDatabases)) {
                String sqlToRun = generatorUnderTest.generateSql(sqlStatement, availableDatabase)[0].toSql();
                try {
                    statement.execute(sqlToRun);
                } catch (Exception e) {
                    System.out.println("Failed to execute against "+availableDatabase.getProductName()+": "+sqlToRun);
                    throw e;

                }
            }
        }
    }

    private String replaceStandardTypes(String convertedSql, Database database) {
        return convertedSql.replaceAll(" int ", " "+database.getColumnType("int", false)+" ");
    }

    private String replaceDatabaseClauses(String convertedSql, Database database) {
        return convertedSql.replaceFirst("auto_increment_clause", database.getAutoIncrementClause());
    }

    private boolean shouldTestDatabase(T sqlStatement, Database database, Class[] includeDatabases, Class[] excludeDatabases) {
    	SqlGenerator<T> generator = SqlGeneratorFactory.getInstance().getBestGenerator(sqlStatement, database);
        if (!generatorUnderTest.isValidGenerator(sqlStatement, database)
                || generatorUnderTest.validate(sqlStatement, database).hasErrors()
                || generator == null 
                || !generator.getClass().equals(generatorUnderTest.getClass())) {
            return false;
        }

        boolean shouldInclude = true;
        if (includeDatabases != null && includeDatabases.length > 0) {
            shouldInclude = Arrays.asList(includeDatabases).contains(database.getClass());
        }

        boolean shouldExclude = false;
        if (excludeDatabases != null && excludeDatabases.length > 0) {
            shouldExclude = Arrays.asList(excludeDatabases).contains(database.getClass());            
        }

        return !shouldExclude && shouldInclude;


    }

    private String replaceEscaping(String expectedSql, Database database) {
        String convertedSql = expectedSql;
        int lastIndex = 0;
        while ((lastIndex = convertedSql.indexOf("[", lastIndex)) >= 0) {
            String objectName = convertedSql.substring(lastIndex+1, convertedSql.indexOf("]", lastIndex));
            convertedSql = convertedSql.replace("["+objectName+"]", database.escapeDatabaseObject(objectName));
            lastIndex++;
        }

        return convertedSql;
    }


}
