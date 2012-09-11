package liquibase.database.core;

import static java.util.Arrays.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import liquibase.exception.DatabaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.statement.SqlStatement;

import org.junit.Test;

public class SybaseDatabaseTest {

	@Test
	public void testIsSybaseProductName() {
		SybaseDatabase database = new SybaseDatabase();
		assertTrue("Sybase SQL Server is a valid product name", database.isSybaseProductName("Sybase SQL Server"));
		assertTrue("sql server is a valid product name", database.isSybaseProductName("sql server"));
		assertTrue("ASE is a valid product name", database.isSybaseProductName("ASE"));
		assertTrue("Adaptive Server Enterprise is a valid product name", database.isSybaseProductName("Adaptive Server Enterprise"));
	}
	
	/**
	 * Configure the {@code Executor} associated with the provided {@code Database}
	 * to return the specified rows.
	 * @param database the database for which to configure an {@code Executor}
	 * @param viewInfoRows the rows to be returned by the {@code Executor}
	 */
	private void configureExecutor(Database database, String... viewInfoRows) {
		Executor executor = createNiceMock(Executor.class);
		try {
			@SuppressWarnings("unchecked")
			Class<String> stringClassMatcher = (Class<String>)anyObject();
			expect(executor.queryForList((SqlStatement)anyObject(), stringClassMatcher)).andReturn(asList(viewInfoRows));
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
		replay(executor);
		ExecutorService.getInstance().setExecutor(database, executor);
	}
	
	@Test
	public void testGetViewDefinitionWhenNoRows() throws Exception {
		SybaseDatabase database = new SybaseDatabase();
		configureExecutor(database);
		
		assertEquals("", database.getViewDefinition(new Schema(new Catalog(null), "dbo"), "view_name"));
	}
	
	@Test
	public void testGetViewDefinitionWhenSingleRow() throws Exception {
		SybaseDatabase database = new SybaseDatabase();
		configureExecutor(database, "foo");
		
		assertEquals("foo", database.getViewDefinition(new Schema(new Catalog(null), "dbo"), "view_name"));
	}
	
	@Test
	public void testGetViewDefinitionWhenMultipleRows() throws Exception {
		SybaseDatabase database = new SybaseDatabase();
		configureExecutor(database, "foo", " bar", " bat");
		
		assertEquals("foo bar bat", database.getViewDefinition(new Schema(new Catalog(null), "dbo"), "view_name"));
	}

	@Test
	public void testGetDatabaseMajorVersionWhenImplemented() throws Exception {
		DatabaseConnection connection = createNiceMock(DatabaseConnection.class);
		expect(connection.getDatabaseMajorVersion()).andReturn(15);
		replay(connection);
		
		SybaseDatabase database = new SybaseDatabase();
		database.setConnection(connection);
		
		assertEquals(15, database.getDatabaseMajorVersion());
	}
	
	@Test
	public void testGetDatabaseMinorVersionWhenImplemented() throws Exception {
		DatabaseConnection connection = createNiceMock(DatabaseConnection.class);
		expect(connection.getDatabaseMinorVersion()).andReturn(5);
		replay(connection);
		
		SybaseDatabase database = new SybaseDatabase();
		database.setConnection(connection);
		
		assertEquals(5, database.getDatabaseMinorVersion());
	}
	
	@Test
	public void testGetDatabaseMajorVersionWhenNotImplemented() throws Exception {
		DatabaseConnection connection = createNiceMock(DatabaseConnection.class);
		expect(connection.getDatabaseMajorVersion()).andThrow(new UnsupportedOperationException());
		replay(connection);
		
		SybaseDatabase database = new SybaseDatabase();
		database.setConnection(connection);
		
		assertEquals(-1, database.getDatabaseMajorVersion());
	}
	
	@Test
	public void testGetDatabaseMinorVersionWhenNotImplemented() throws Exception {
		DatabaseConnection connection = createNiceMock(DatabaseConnection.class);
		expect(connection.getDatabaseMinorVersion()).andThrow(new UnsupportedOperationException());
		replay(connection);
		
		SybaseDatabase database = new SybaseDatabase();
		database.setConnection(connection);
		
		assertEquals(-1, database.getDatabaseMinorVersion());
	}

}
