package liquibase.statement;

import liquibase.change.ColumnConfig;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.PreparedStatementFactory;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.resource.ResourceAccessor;
import liquibase.structure.core.Column;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UpdateExecutablePreparedStatementTest {

    @Mock
    private ChangeSet changeSet;

    @Mock
    private ResourceAccessor resourceAccessor;

    @Mock
    private JdbcConnection connection;

    @Mock
    private PreparedStatement ps;

    private Database database;

    private PreparedStatementFactory preparedStatementFactory;

    @Before
    public void setUp() throws Exception {
        given(connection.prepareStatement(anyString())).willReturn(ps);
        database = new MSSQLDatabase();
        preparedStatementFactory = new PreparedStatementFactory(connection);
    }

    @Test
    public void testContinueOnError() {
        assertFalse(new UpdateExecutablePreparedStatement(database,
                "catalogName",
                "schemaName",
                "tableName",
                new ArrayList<ColumnConfig>(),
                changeSet,
                resourceAccessor).continueOnError());
    }

    @Test
    public void testExecuteWithParamPlaceholders() throws Exception {
        // given
        UpdateExecutablePreparedStatement statement = new UpdateExecutablePreparedStatement(
                database,
                null,
                null,
                "DATABASECHANGELOG",
                new ArrayList<ColumnConfig>(asList(
                        new ColumnConfig()
                                .setName("MD5SUM")
                                .setValue("7:e27bf9c0c2313160ef960a15d44ced47"))),
                changeSet,
                resourceAccessor)
                .setWhereClause(
                        database.escapeObjectName("ID", Column.class) + " = ? " +
                                "AND " + database.escapeObjectName("AUTHOR", Column.class) + " = ? " +
                                "AND " + database.escapeObjectName("FILENAME", Column.class) + " = ?")
                .addWhereParameters(
                        "SYPA: AUTO_START tüüp INT -> TEXT, vaartus 0 00 17 * * ?",
                        "martin",
                        "db/changelog.xml");

        // when
        statement.execute(preparedStatementFactory);

        // then
        verify(connection).prepareStatement(
                "UPDATE DATABASECHANGELOG " +
                        "SET MD5SUM = ? " +
                        "WHERE ID = N'SYPA: AUTO_START tüüp INT -> TEXT, vaartus 0 00 17 * * ?' " +
                        "AND AUTHOR = 'martin' " +
                        "AND FILENAME = 'db/changelog.xml'");
        verify(ps).setString(1, "7:e27bf9c0c2313160ef960a15d44ced47");
    }

    @Test
    public void testExecuteWithNameValuePlaceholderPairs() throws Exception {
        // given
        UpdateExecutablePreparedStatement statement = new UpdateExecutablePreparedStatement(
                database,
                null,
                null,
                "DATABASECHANGELOG",
                new ArrayList<ColumnConfig>(asList(
                        new ColumnConfig()
                                .setName("MD5SUM")
                                .setValue("7:e27bf9c0c2313160ef960a15d44ced47"))),
                changeSet,
                resourceAccessor)
                .setWhereClause(":name = :value AND :name = :value AND :name = :value")
                .addWhereColumnName("ID")
                .addWhereColumnName("AUTHOR")
                .addWhereColumnName("FILENAME")
                .addWhereParameters(
                        "SYPA: AUTO_START tüüp INT -> TEXT, vaartus 0 00 17 * * ?",
                        "martin",
                        "db/changelog.xml");

        // when
        statement.execute(preparedStatementFactory);

        // then
        verify(connection).prepareStatement(
                "UPDATE DATABASECHANGELOG " +
                        "SET MD5SUM = ? " +
                        "WHERE ID = N'SYPA: AUTO_START tüüp INT -> TEXT, vaartus 0 00 17 * * ?' " +
                        "AND AUTHOR = 'martin' " +
                        "AND FILENAME = 'db/changelog.xml'");
        verify(ps).setString(1, "7:e27bf9c0c2313160ef960a15d44ced47");
    }

    @Test
    public void testExecute() throws DatabaseException, SQLException {

        Database database = new OracleDatabase();

        ColumnConfig columnConfig1 = new ColumnConfig();
        columnConfig1.setName("column1");
        columnConfig1.setAutoIncrement(true);
        columnConfig1.setValue("value1");

        ColumnConfig columnConfig2 = new ColumnConfig();
        columnConfig2.setName("column2");
        columnConfig2.setValue("value2");

        ColumnConfig columnConfig3 = new ColumnConfig();
        columnConfig3.setName("column3");
        columnConfig3.setValue("value3");

        ColumnConfig columnConfig4 = new ColumnConfig();
        columnConfig4.setName("column4");
        columnConfig4.setValue("value4");

        UpdateExecutablePreparedStatement updateExecutablePreparedStatement = new UpdateExecutablePreparedStatement(database,
                "catalogName",
                "schemaName",
                "tableName",
                Arrays.asList(columnConfig1, columnConfig2, columnConfig3, columnConfig4),
                changeSet,
                resourceAccessor);

        updateExecutablePreparedStatement.execute(preparedStatementFactory);

        verify(connection).prepareStatement(
                "UPDATE catalogName.tableName " +
                        "SET column1 = ?, " +
                        "column2 = ?, " +
                        "column3 = ?, " +
                        "column4 = ?");
        verify(ps).setString(1, "value1");
        verify(ps).setString(2, "value2");
        verify(ps).setString(3, "value3");
        verify(ps).setString(4, "value4");
        verify(ps).execute();
        verifyNoMoreInteractions(ps);
    }

	@Test
	public void testExecuteWithClobAndComputedValue() throws Exception {
        // given
        UpdateExecutablePreparedStatement statement = new UpdateExecutablePreparedStatement(
                        database,
                        null,
                        null,
                        "DATABASECHANGELOG",
                        new ArrayList<ColumnConfig>(asList(
                                new ColumnConfig()
                                        .setName("MD5SUM")
                                        .setValue("7:e27bf9c0c2313160ef960a15d44ced47"),
                                new ColumnConfig()
                                        .setName("DATEEXECUTED")
                                        .setValueDate("GETDATE()"))),
                        changeSet,
                        resourceAccessor);

        // when
        statement.execute(preparedStatementFactory);

        // then
        verify(connection).prepareStatement("UPDATE DATABASECHANGELOG SET MD5SUM = ?, DATEEXECUTED = GETDATE()");
		verify(ps).setString(1, "7:e27bf9c0c2313160ef960a15d44ced47");
		verify(ps, never()).setNull(eq(2), anyInt());
	}

    @Test
    public void testExecute_ValueComputed() throws DatabaseException, SQLException {

        Database database = new OracleDatabase();

        ColumnConfig columnConfig1 = new ColumnConfig();
        columnConfig1.setName("column1");
        columnConfig1.setAutoIncrement(true);
        columnConfig1.setValue("value1");

        ColumnConfig columnConfig2 = new ColumnConfig();
        columnConfig2.setName("column2");
        columnConfig2.setValue("value2");

        ColumnConfig columnConfig3 = new ColumnConfig();
        columnConfig3.setName("column3");
        columnConfig3.setValueComputed(new DatabaseFunction("select * from abc where x=y"));

        ColumnConfig columnConfig4 = new ColumnConfig();
        columnConfig4.setName("column4");
        columnConfig4.setValue("value4");

        UpdateExecutablePreparedStatement updateExecutablePreparedStatement = new UpdateExecutablePreparedStatement(database,
                "catalogName",
                "schemaName",
                "tableName",
                Arrays.asList(columnConfig1, columnConfig2, columnConfig3, columnConfig4),
                changeSet,
                resourceAccessor);

        updateExecutablePreparedStatement.execute(preparedStatementFactory);

        verify(connection).prepareStatement(
                "UPDATE catalogName.tableName " +
                        "SET column1 = ?, " +
                        "column2 = ?, " +
                        "column3 = select * from abc where x=y, " +
                        "column4 = ?");
        verify(ps).setString(1, "value1");
        verify(ps).setString(2, "value2");
        verify(ps).setString(3, "value4");
        verify(ps).execute();
        verifyNoMoreInteractions(ps);
    }
}
