package liquibase.statement

import liquibase.change.ColumnConfig
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.exception.DatabaseException
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.CompositeResourceAccessor
import liquibase.resource.FileSystemResourceAccessor
import liquibase.resource.ResourceAccessor
import spock.lang.Ignore
import spock.lang.Specification

import java.sql.SQLException

class ExecutablePreparedStatementTest extends Specification {

    @Ignore
	def testValueBlobFileFromClassLoader() throws DatabaseException, SQLException {
        expect:
        ColumnConfig columnConfig = new ColumnConfig()

        String valueBlobFile = "../unicode-file.txt"
        columnConfig.setValueBlobFile(valueBlobFile)

        List<ColumnConfig> columns = Arrays.asList(columnConfig)

        ChangeSet changeSet = createMock(ChangeSet.class)
        DatabaseChangeLog changeLog = createMock(DatabaseChangeLog.class)
        expect(changeLog.getPhysicalFilePath()).andReturn("liquibase/util/foo/")
        replay(changeLog)
        expect(changeSet.getChangeLog()).andReturn(changeLog)
        replay(changeSet)

        assertSetBinaryStream(columns, changeSet)
    }

    @Ignore
	def testValueBlobFileFromFile() throws DatabaseException, SQLException {
        expect:
        ColumnConfig columnConfig = new ColumnConfig()

        String valueBlobFile = "unicode-file.txt"
        columnConfig.setValueBlobFile(valueBlobFile)

        List<ColumnConfig> columns = Arrays.asList(columnConfig)

        ChangeSet changeSet = createMock(ChangeSet.class)
        DatabaseChangeLog changeLog = createMock(DatabaseChangeLog.class)
        expect(changeLog.getPhysicalFilePath()).andReturn("src/test/resources/liquibase/util/")
        replay(changeLog)
        expect(changeSet.getChangeLog()).andReturn(changeLog)
        replay(changeSet)

        assertSetBinaryStream(columns, changeSet)
    }

    @Ignore
	def testValueClobFileFromClassLoader() throws DatabaseException, SQLException {
        expect:
        ColumnConfig columnConfig = new ColumnConfig()

        String valueClobFile = "unicode-file.txt"
        columnConfig.setValueClobFile(valueClobFile)
        columnConfig.setEncoding("UTF-8")

        List<ColumnConfig> columns = Arrays.asList(columnConfig)

        ChangeSet changeSet = createMock(ChangeSet.class)
        DatabaseChangeLog changeLog = createMock(DatabaseChangeLog.class)
        expect(changeLog.getPhysicalFilePath()).andReturn("liquibase/util/")
        replay(changeLog)
        expect(changeSet.getChangeLog()).andReturn(changeLog)
        replay(changeSet)

        assertSetCharacterStream(columns, changeSet)
    }

    @Ignore
	def testValueClobFileFromFile() throws DatabaseException, SQLException {
        expect:
        ColumnConfig columnConfig = new ColumnConfig()

        String valueClobFile = "unicode-file.txt"
        columnConfig.setValueClobFile(valueClobFile)
        columnConfig.setEncoding("UTF-8")

        List<ColumnConfig> columns = Arrays.asList(columnConfig)

        ChangeSet changeSet = createMock(ChangeSet.class)
        DatabaseChangeLog changeLog = createMock(DatabaseChangeLog.class)
        expect(changeLog.getPhysicalFilePath()).andReturn("src/test/resources/liquibase/util/")
        replay(changeLog)
        expect(changeSet.getChangeLog()).andReturn(changeLog)
        replay(changeSet)

        assertSetCharacterStream(columns, changeSet)
    }

    /**
	 * Create a test context resource accessor.
	 * @return
	 */
	private ResourceAccessor createResourceAccessor() {
		ResourceAccessor resourceAccessor = new CompositeResourceAccessor(
				new ClassLoaderResourceAccessor(),
				new FileSystemResourceAccessor(),
                new ClassLoaderResourceAccessor(Thread.currentThread().getContextClassLoader()))

        return resourceAccessor
    }
}
