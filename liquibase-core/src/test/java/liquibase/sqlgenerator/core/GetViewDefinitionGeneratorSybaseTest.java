package liquibase.sqlgenerator.core;

import liquibase.RuntimeEnvironment;
import liquibase.action.Action;
import liquibase.database.core.SybaseDatabase;
import liquibase.executor.ExecutionOptions;
import liquibase.statement.core.GetViewDefinitionStatement;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GetViewDefinitionGeneratorSybaseTest {

	@Test
	public void testGenerateSqlForDefaultSchema() {
		GetViewDefinitionGeneratorSybase generator = new GetViewDefinitionGeneratorSybase();
		GetViewDefinitionStatement statement = new GetViewDefinitionStatement(null, null, "view_name");
		Action[] actions = generator.generateActions(statement, new ExecutionOptions(new RuntimeEnvironment(new SybaseDatabase())), null);
		assertEquals(1, actions.length);
		assertEquals("select text from syscomments where id = object_id('dbo.view_name') order by colid", actions[0].describe());
	}
	
	@Test
	public void testGenerateSqlForNamedSchema() {
		GetViewDefinitionGeneratorSybase generator = new GetViewDefinitionGeneratorSybase();
		GetViewDefinitionStatement statement = new GetViewDefinitionStatement(null, "owner", "view_name");
		Action[] actions = generator.generateActions(statement, new ExecutionOptions(new RuntimeEnvironment(new SybaseDatabase())), null);
		assertEquals(1, actions.length);
		assertEquals("select text from syscomments where id = object_id('OWNER.view_name') order by colid", actions[0].describe());
	}

}
