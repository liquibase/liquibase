/**
 * 
 */
package liquibase.sqlgenerator.core;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.exception.LiquibaseException;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.InsertOrUpdateStatement;

/**
 * @author luciano.boschi
 *
 */
public class InsertOrUpdateGeneratorMySQLTest {

    private static final String CATALOG_NAME = "mycatalog";
    private static final String SCHEMA_NAME = "myschema";
    private static final String TABLE_NAME = "MYTABLE";
    //private static final String SEQUENCE_NAME = "my_sequence";

	/**
	 * Test method for {@link liquibase.sqlgenerator.core.InsertOrUpdateGeneratorMySQL#getUpdateStatement(liquibase.statement.core.InsertOrUpdateStatement, liquibase.database.Database, java.lang.String, liquibase.sqlgenerator.SqlGeneratorChain)}.
	 * @throws LiquibaseException 
	 */
	@Test
	public void testGetUpdateStatement_notOnlyUpdate() throws LiquibaseException {
		final InsertOrUpdateGeneratorMySQL generator = new InsertOrUpdateGeneratorMySQL();
		
		final InsertOrUpdateStatement insertOrUpdateStatement = new InsertOrUpdateStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME, "pk1");
		final Database database = new MySQLDatabase();
		final SqlGeneratorChain sqlGeneratorChain = null;
		
		ColumnConfig columnConfig;
        columnConfig = new ColumnConfig();
        columnConfig.setValue("value0");
        columnConfig.setName("col0");
        insertOrUpdateStatement.addColumn(columnConfig);
        columnConfig = new ColumnConfig();
        columnConfig.setValue("keyvalue1");
        columnConfig.setName("pk1");
        insertOrUpdateStatement.addColumn(columnConfig);
		final String whereClause = generator.getWhereClause(insertOrUpdateStatement, database);
		
		String result = generator.getUpdateStatement(insertOrUpdateStatement, database, whereClause, sqlGeneratorChain);
		assertEquals("", result);
	}

	/**
	 * Test method for {@link liquibase.sqlgenerator.core.InsertOrUpdateGeneratorMySQL#getUpdateStatement(liquibase.statement.core.InsertOrUpdateStatement, liquibase.database.Database, java.lang.String, liquibase.sqlgenerator.SqlGeneratorChain)}.
	 * @throws LiquibaseException 
	 */
	@Test
	public void testGetUpdateStatement_onlyUpdate() throws LiquibaseException {
		final InsertOrUpdateGeneratorMySQL generator = new InsertOrUpdateGeneratorMySQL();
		
		final InsertOrUpdateStatement insertOrUpdateStatement = new InsertOrUpdateStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME, "pk1", true);
		final Database database = new MySQLDatabase();
		final SqlGeneratorChain sqlGeneratorChain = null;
		
		ColumnConfig columnConfig;
        columnConfig = new ColumnConfig();
        columnConfig.setValue("value0");
        columnConfig.setName("col0");
        insertOrUpdateStatement.addColumn(columnConfig);
        columnConfig = new ColumnConfig();
        columnConfig.setValue("keyvalue1");
        columnConfig.setName("pk1");
        insertOrUpdateStatement.addColumn(columnConfig);
		final String whereClause = generator.getWhereClause(insertOrUpdateStatement, database);
		
		String result = generator.getUpdateStatement(insertOrUpdateStatement, database, whereClause, sqlGeneratorChain);
		assertEquals(String.format("UPDATE %s.%s SET %s = '%s' WHERE %s = '%s';\n", CATALOG_NAME, TABLE_NAME, "col0", "value0", "pk1", "keyvalue1"), result);
	}

	/**
	 * Test method for {@link liquibase.sqlgenerator.core.InsertOrUpdateGenerator#generateSql(liquibase.statement.core.InsertOrUpdateStatement, liquibase.database.Database, liquibase.sqlgenerator.SqlGeneratorChain)}.
	 */
	@Test
	public void testGenerateSql_notOnlyUpdate() {
		final InsertOrUpdateGeneratorMySQL generator = new InsertOrUpdateGeneratorMySQL();
		
		final InsertOrUpdateStatement insertOrUpdateStatement = new InsertOrUpdateStatement(CATALOG_NAME, SCHEMA_NAME, TABLE_NAME, "pk1");
		final Database database = new MySQLDatabase();
		final SqlGeneratorChain sqlGeneratorChain = null;
		
		ColumnConfig columnConfig;
        columnConfig = new ColumnConfig();
        columnConfig.setValue("keyvalue1");
        columnConfig.setName("pk1");
        insertOrUpdateStatement.addColumn(columnConfig);
        columnConfig = new ColumnConfig();
        columnConfig.setValue("value0");
        columnConfig.setName("col0");
        insertOrUpdateStatement.addColumn(columnConfig);
		//final String whereClause = generator.getWhereClause(insertOrUpdateStatement, database);
		
		Sql[] results = generator.generateSql(insertOrUpdateStatement, database, sqlGeneratorChain);
		assertThat(results, is(arrayWithSize(1)));
        assertEquals(String.format("INSERT INTO %s.%s (%s, %s) VALUES ('%s', '%s')\nON DUPLICATE KEY UPDATE col0 = 'value0'", CATALOG_NAME, TABLE_NAME, "pk1", "col0", "keyvalue1", "value0"), results[0].toSql());
	}

}
