package liquibase.sqlgenerator.core;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.sql.Sql;
import liquibase.statement.core.GetViewDefinitionStatement;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GetViewDefinitionGeneratorSnowflakeTest {

	@Test
	public void testGenerateSqlForCamelCaseView() {
		GetViewDefinitionGeneratorSnowflake generator = new GetViewDefinitionGeneratorSnowflake();
		GetViewDefinitionStatement statement = new GetViewDefinitionStatement("DEV_LZ", "DBS", "EDW_DBS_Estimate");
		Sql[] sql = generator.generateSql(statement, new SnowflakeDatabase(), null);
		assertEquals(1, sql.length);
		assertEquals("SELECT GET_DDL('VIEW', 'DEV_LZ.DBS.EDW_DBS_Estimate', TRUE)", sql[0].toSql());
	}

}
