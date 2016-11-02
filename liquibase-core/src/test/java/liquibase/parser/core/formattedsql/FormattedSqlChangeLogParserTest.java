package liquibase.parser.core.formattedsql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;

import liquibase.change.Change;
import liquibase.change.core.EmptyChange;
import liquibase.change.core.RawSQLChange;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.ChangeLogParseException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.sdk.database.MockDatabase;
import liquibase.statement.SqlStatement;

import org.junit.Test;

public class FormattedSqlChangeLogParserTest {
	private FormattedSqlChangeLogParser parser = new FormattedSqlChangeLogParser();
	private Database db = new MockDatabase();
	private DatabaseChangeLog changeLog;

	@Test
	public void shouldParse() throws ChangeLogParseException, RollbackImpossibleException {
		changeLog = parse("rollbackChangeLog.sql");
		//verifyMultiLine();
		//assertEmptyRollback();
		//verifyMultiRollback();
		assertEquals(changeLog.getChangeSets().size(), 2);
	}

	@Test
	public void shouldFailNoSql() {
		try {
			changeLog = parse("noSqlChangeLog.sql");
			fail("Expected exception");
		} catch (ChangeLogParseException e) {
			assertEquals("No SQL for changeset test-path::multi-rollback::test-author", e.getMessage());
		}
	}

//	protected void verifyMultiLine() {
//		ChangeSet cs = getChangeSet("multi-line");
//		Change[] rollbacks = cs.getRollBackChanges();
//		assertEquals(rollbacks.length, 1);
//		assertEquals(rollbacks[0].getClass(), RawSQLChange.class);
//		SqlStatement[] sqlBlock;
//		sqlBlock = rollbacks[0].generateStatements(db);
//		assertEquals(sqlBlock.length, 2);
//		assertTrue(cs.getComments().length() > 0);
//	}

//	protected void assertEmptyRollback() {
//		ChangeSet cs = getChangeSet("empty-rollback");
//		Change[] rollbacks = cs.getRollBackChanges();
//		assertEquals(1, rollbacks.length);
//		assertEquals(rollbacks[0].getClass(), EmptyChange.class);
//		SqlStatement[] sqlBlock;
//		sqlBlock = rollbacks[0].generateStatements(db);
//		assertEquals(0, sqlBlock.length);
//		assertFalse(cs.isRunInTransaction());
//	}

//	protected void verifyMultiRollback() {
//		ChangeSet cs = getChangeSet("multi-rollback");
//		Change[] rollbacks = cs.getRollBackChanges();
//		assertEquals(rollbacks.length, 1);
//		assertEquals(rollbacks[0].getClass(), RawSQLChange.class);
//		SqlStatement[] sqlBlock;
//		sqlBlock = rollbacks[0].generateStatements(db);
//		assertEquals(sqlBlock.length, 3);
//	}

	/**
	 * @param id
	 *            TODO
	 * @return
	 */
	protected ChangeSet getChangeSet(String id) {
		ChangeSet r = changeLog.getChangeSet("test-path", "test-author", id);
		assertNotNull("Changeset:" + id, r);
		System.out.println("testing " + r);
		return r;
	}

	private DatabaseChangeLog parse(String name) throws ChangeLogParseException {
		URL target = getClass().getResource(name);
		assertNotNull(target);
		return parser.parse(target.getFile(), new ChangeLogParameters(), new FileSystemResourceAccessor());
	}
}
