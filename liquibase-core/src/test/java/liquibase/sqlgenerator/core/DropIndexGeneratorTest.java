package liquibase.sqlgenerator.core;

import java.util.SortedSet;
import java.util.TreeSet;

import liquibase.RuntimeEnvironment;
import liquibase.actiongenerator.ActionGenerator;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.executor.ExecutionOptions;
import liquibase.action.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropIndexStatement;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class DropIndexGeneratorTest {
//    @Test
//    public void execute_defaultSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new DropIndexStatement(IDX_NAME, null, TABLE_NAME)) {
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNotNull(snapshot.getIndex(IDX_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getIndex(IDX_NAME));
//                    }
//
//                });
//    }
//
//    //todo: issues with schemas on some databases
////    @Test
////    public void execute_altSchema() throws Exception {
////        new DatabaseTestTemplate().testOnAvailableDatabases(
////                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new DropIndexStatement(ALT_IDX_NAME, TestContext.ALT_SCHEMA, TABLE_NAME)) {
////
////                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
////                        //todo: how do we assert indexes within a schema snapshot?
//////                        assertNotNull(snapshot.getIndex(ALT_IDX_NAME));
////                    }
////
////                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
////                        //todo: how do we assert indexes within a schema snapshot?
//////                        assertNull(snapshot.getIndex(ALT_IDX_NAME));
////                    }
////
////                });
////    }


	@Test
	public void shouldDropIndexInPostgreSQL() throws Exception {
		DropIndexGenerator dropIndexGenerator = new DropIndexGenerator();
		DropIndexStatement statement = new DropIndexStatement("indexName", "defaultCatalog", "defaultSchema", "aTable", null);
		Database database = new PostgresDatabase();
		SortedSet<ActionGenerator> sqlGenerators = new TreeSet<ActionGenerator>();
		SqlGeneratorChain sqlGenerationChain = new SqlGeneratorChain(new ActionGeneratorChain(sqlGenerators));

        ExecutionOptions options = new ExecutionOptions(new RuntimeEnvironment(database));

		Sql[] sqls = dropIndexGenerator.generateSql(statement, options, sqlGenerationChain);
		assertEquals("DROP INDEX \"defaultSchema\".\"indexName\"", sqls[0].toSql());

		statement = new DropIndexStatement("index_name", "default_catalog", "default_schema", "a_table", null);
		sqls = dropIndexGenerator.generateSql(statement, options , sqlGenerationChain);
		assertEquals("DROP INDEX default_schema.index_name", sqls[0].toSql());

		statement = new DropIndexStatement("index_name", null, null, "a_table", null);
		sqls = dropIndexGenerator.generateSql(statement, options, sqlGenerationChain);
		assertEquals("DROP INDEX index_name", sqls[0].toSql());
	}
}
