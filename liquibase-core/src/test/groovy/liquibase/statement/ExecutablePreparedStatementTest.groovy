package liquibase.statement

import liquibase.change.ColumnConfig
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.database.PreparedStatementFactory
import liquibase.sdk.database.MockDatabase
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.DatabaseException
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.CompositeResourceAccessor
import liquibase.resource.FileSystemResourceAccessor
import liquibase.resource.ResourceAccessor
import org.easymock.Capture
import org.easymock.IAnswer
import org.junit.Assert
import spock.lang.Ignore
import spock.lang.Specification

import java.sql.PreparedStatement
import java.sql.SQLException

import static org.easymock.EasyMock.*
import static org.easymock.classextension.EasyMock.createMock
import static org.easymock.classextension.EasyMock.replay

public class ExecutablePreparedStatementTest extends Specification {

    @Ignore
	def testValueBlobFileFromClassLoader() throws DatabaseException, SQLException {
        expect:
		ColumnConfig columnConfig = new ColumnConfig();
		
		String valueBlobFile = "../unicode-file.txt";
		columnConfig.setValueBlobFile(valueBlobFile);
		
		List<ColumnConfig> columns = Arrays.asList(columnConfig);
		
		ChangeSet changeSet = createMock(ChangeSet.class);
		DatabaseChangeLog changeLog = createMock(DatabaseChangeLog.class);
		expect(changeLog.getPhysicalFilePath()).andReturn("liquibase/util/foo/");
		replay(changeLog);
		expect(changeSet.getChangeLog()).andReturn(changeLog);
		replay(changeSet);
		
		assertSetBinaryStream(columns, changeSet);
	}

    @Ignore
	def testValueBlobFileFromFile() throws DatabaseException, SQLException {
        expect:
		ColumnConfig columnConfig = new ColumnConfig();
		
		String valueBlobFile = "unicode-file.txt";
		columnConfig.setValueBlobFile(valueBlobFile);
		
		List<ColumnConfig> columns = Arrays.asList(columnConfig);
		
		ChangeSet changeSet = createMock(ChangeSet.class);
		DatabaseChangeLog changeLog = createMock(DatabaseChangeLog.class);
		expect(changeLog.getPhysicalFilePath()).andReturn("src/test/resources/liquibase/util/");
		replay(changeLog);
		expect(changeSet.getChangeLog()).andReturn(changeLog);
		replay(changeSet);
		
		assertSetBinaryStream(columns, changeSet);
	}

	protected void assertSetBinaryStream(List<ColumnConfig> columns, ChangeSet changeSet)
			throws SQLException, DatabaseException {
		
		InsertExecutablePreparedStatement statement =
				new InsertExecutablePreparedStatement(
						new MockDatabase(), "catalog", "schema", "table", columns, changeSet, createResourceAccessor());
		
		PreparedStatement stmt = createMock(PreparedStatement.class);

		final Capture<Integer> index = new Capture<Integer>();
		final Capture<InputStream> inStream = new Capture<InputStream>();
		final Capture<Integer> length = new Capture<Integer>();
		stmt.setBinaryStream(capture(index), capture(inStream), capture(length));
		expectLastCall().andAnswer(new IAnswer<Object>() {
			@Override
			public Object answer() throws Throwable {
				Assert.assertEquals(new Integer(1), index.getValue());
				Assert.assertNotNull(inStream.getValue());
				Assert.assertTrue(inStreamgetValue() instanceof BufferedInputStream);
				Assert.assertEquals(new Integer(50), length.getValue());
				return null;
			}
		});
		expect(stmt.execute()).andReturn(true);
		replay(stmt);
		
		JdbcConnection connection = createMock(JdbcConnection.class);
		expect(connection.prepareStatement("INSERT INTO schema.table(null) VALUES(?)")).andReturn(stmt);
		replay(connection);
		
		statement.execute(new PreparedStatementFactory(connection));
	}

    @Ignore
	def testValueClobFileFromClassLoader() throws DatabaseException, SQLException {
        expect:
		ColumnConfig columnConfig = new ColumnConfig();
		
		String valueClobFile = "unicode-file.txt";
		columnConfig.setValueClobFile(valueClobFile);
		columnConfig.setEncoding("UTF-8");
		
		List<ColumnConfig> columns = Arrays.asList(columnConfig);
		
		ChangeSet changeSet = createMock(ChangeSet.class);
		DatabaseChangeLog changeLog = createMock(DatabaseChangeLog.class);
		expect(changeLog.getPhysicalFilePath()).andReturn("liquibase/util/");
		replay(changeLog);
		expect(changeSet.getChangeLog()).andReturn(changeLog);
		replay(changeSet);
		
		assertSetCharacterStream(columns, changeSet);
	}

    @Ignore
	def testValueClobFileFromFile() throws DatabaseException, SQLException {
        expect:
		ColumnConfig columnConfig = new ColumnConfig();
		
		String valueClobFile = "unicode-file.txt";
		columnConfig.setValueClobFile(valueClobFile);
		columnConfig.setEncoding("UTF-8");
		
		List<ColumnConfig> columns = Arrays.asList(columnConfig);
		
		ChangeSet changeSet = createMock(ChangeSet.class);
		DatabaseChangeLog changeLog = createMock(DatabaseChangeLog.class);
		expect(changeLog.getPhysicalFilePath()).andReturn("src/test/resources/liquibase/util/");
		replay(changeLog);
		expect(changeSet.getChangeLog()).andReturn(changeLog);
		replay(changeSet);
		
		assertSetCharacterStream(columns, changeSet);
	}

	protected void assertSetCharacterStream(List<ColumnConfig> columns, ChangeSet changeSet)
			throws SQLException, DatabaseException {
		
		InsertExecutablePreparedStatement statement =
				new InsertExecutablePreparedStatement(
						new MockDatabase(),
						"catalog", "schema", "table", columns, changeSet, createResourceAccessor());
		
		PreparedStatement stmt = createMock(PreparedStatement.class);

		final Capture<Integer> index = new Capture<Integer>();
		final Capture<Reader> reader = new Capture<Reader>();
		final Capture<Integer> length = new Capture<Integer>();
		stmt.setCharacterStream(capture(index), capture(reader), capture(length));
		expectLastCall().andAnswer(new IAnswer<Object>() {
			@Override
			public Object answer() throws Throwable {
				Assert.assertEquals(new Integer(1), index.getValue());
				Assert.assertNotNull(reader.getValue());
				Assert.assertTrue(reader.getValue() instanceof BufferedReader);
				Assert.assertEquals(new Integer(39), length.getValue());
				return null;
			}
		});
		expect(stmt.execute()).andReturn(true);
		replay(stmt);
		
		JdbcConnection connection = createMock(JdbcConnection.class);
		expect(connection.prepareStatement("INSERT INTO schema.table(null) VALUES(?)")).andReturn(stmt);
		replay(connection);
		
		statement.execute(new PreparedStatementFactory(connection));
	}
	
	/**
	 * Create a test context resource accessor.
	 * @return
	 */
	private ResourceAccessor createResourceAccessor() {
		ResourceAccessor resourceAccessor = new CompositeResourceAccessor(
				new ClassLoaderResourceAccessor(),
				new FileSystemResourceAccessor(),
				new ClassLoaderResourceAccessor(Thread.currentThread().getContextClassLoader()));
		
		return resourceAccessor;
	}
}
