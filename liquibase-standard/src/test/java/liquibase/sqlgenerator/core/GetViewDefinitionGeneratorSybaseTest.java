package liquibase.sqlgenerator.core;

import liquibase.database.core.SybaseDatabase;
import liquibase.sql.Sql;
import liquibase.statement.core.GetViewDefinitionStatement;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GetViewDefinitionGeneratorSybaseTest {

	@Test
	public void testGenerateSqlForDefaultSchema() {
		GetViewDefinitionGeneratorSybase generator = new GetViewDefinitionGeneratorSybase();
		GetViewDefinitionStatement statement = new GetViewDefinitionStatement(null, null, "view_name");
		Sql[] sql = generator.generateSql(statement, new SybaseDatabase(), null);
		assertEquals(1, sql.length);
		assertEquals("select text from syscomments where id = object_id('dbo.view_name') order by colid", sql[0].toSql());
	}
	
	@Test
	public void testGenerateSqlForNamedSchema() {
		GetViewDefinitionGeneratorSybase generator = new GetViewDefinitionGeneratorSybase();
		GetViewDefinitionStatement statement = new GetViewDefinitionStatement(null, "owner", "view_name");
		Sql[] sql = generator.generateSql(statement, new SybaseDatabase(), null);
		assertEquals(1, sql.length);
		assertEquals("select text from syscomments where id = object_id('OWNER.view_name') order by colid", sql[0].toSql());
	}

}
