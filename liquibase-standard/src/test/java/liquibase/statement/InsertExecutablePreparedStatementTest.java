package liquibase.statement;

import java.sql.PreparedStatement;
import java.util.ArrayList;

import java.sql.SQLException;
import liquibase.change.ColumnConfig;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.PreparedStatementFactory;
import liquibase.database.core.H2Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.resource.ResourceAccessor;
import liquibase.sql.visitor.ReplaceSqlVisitor;
import liquibase.sql.visitor.SqlVisitor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InsertExecutablePreparedStatementTest {

    @Mock
    private ChangeSet changeSet;

    private List<SqlVisitor> sqlVisitors = new ArrayList<>();

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
        database = new MSSQLDatabase();
        preparedStatementFactory = new PreparedStatementFactory(connection);
        when(changeSet.getSqlVisitors()).thenReturn(sqlVisitors);
        when(connection.prepareStatement(any(String.class))).thenReturn(ps);
    }

    @Test
    public void testContinueOnError() {
        assertFalse(new InsertExecutablePreparedStatement(database,
                "catalogName",
                "schemaName",
                "tableName",
                new ArrayList<ColumnConfig>(),
                changeSet,
                resourceAccessor).continueOnError());
    }

    @Test
    public void testExecute() throws DatabaseException, SQLException {

        ColumnConfig columnConfig1 = new ColumnConfig();
        columnConfig1.setName("column1");
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

        InsertExecutablePreparedStatement insertExecutablePreparedStatement = new InsertExecutablePreparedStatement(database,
                "catalogName",
                "schemaName",
                "tableName",
                Arrays.asList(columnConfig1, columnConfig2, columnConfig3, columnConfig4),
                changeSet,
                resourceAccessor);

        insertExecutablePreparedStatement.execute(preparedStatementFactory);

        verify(connection).prepareStatement(
                "INSERT INTO catalogName.schemaName.tableName(column1, column2, column3, column4) VALUES(?, ?, ?, ?)");
        verify(ps).setString(1, "value1");
        verify(ps).setString(2, "value2");
        verify(ps).setString(3, "value3");
        verify(ps).setString(4, "value4");
        verify(ps).execute();
        verifyNoMoreInteractions(ps);
    }

    @Test
    public void testExecute_WithAutoIncrement() throws DatabaseException, SQLException {

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

        InsertExecutablePreparedStatement insertExecutablePreparedStatement = new InsertExecutablePreparedStatement(database,
                "catalogName",
                "schemaName",
                "tableName",
                Arrays.asList(columnConfig1, columnConfig2, columnConfig3, columnConfig4),
                changeSet,
                resourceAccessor);

        insertExecutablePreparedStatement.execute(preparedStatementFactory);

        verify(connection).prepareStatement(
                "INSERT INTO catalogName.schemaName.tableName(column2, column3, column4) VALUES(?, ?, ?)");
        verify(ps).setString(1, "value2");
        verify(ps).setString(2, "value3");
        verify(ps).setString(3, "value4");
        verify(ps).execute();
        verifyNoMoreInteractions(ps);
    }

    @Test
    public void testExecute_ValueComputed() throws DatabaseException, SQLException {

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

        InsertExecutablePreparedStatement insertExecutablePreparedStatement = new InsertExecutablePreparedStatement(database,
                "catalogName",
                "schemaName",
                "tableName",
                Arrays.asList(columnConfig1, columnConfig2, columnConfig3, columnConfig4),
                changeSet,
                resourceAccessor);

        insertExecutablePreparedStatement.execute(preparedStatementFactory);

        verify(connection).prepareStatement(
                "INSERT INTO catalogName.schemaName.tableName(column2, column3, column4) VALUES(?, select * from abc where x=y, ?)");
        verify(ps).setString(1, "value2");
        verify(ps).setString(2, "value4");
        verify(ps).execute();
        verifyNoMoreInteractions(ps);
    }

    @Test
    public void testExecuteWithClobAndComputedValue() throws Exception {
        // given
        InsertExecutablePreparedStatement statement = new InsertExecutablePreparedStatement(
                database,
                null,
                null,
                "DATABASECHANGELOG",
                Arrays.asList(
                        new ColumnConfig()
                                .setName("MD5SUM")
                                .setValue("7:e27bf9c0c2313160ef960a15d44ced47"),
                        new ColumnConfig()
                                .setName("DATEEXECUTED")
                                .setValueDate("GETDATE()")),
                changeSet,
                resourceAccessor);

        // when
        statement.execute(preparedStatementFactory);

        // then
        verify(connection).prepareStatement("INSERT INTO DATABASECHANGELOG(MD5SUM, DATEEXECUTED) VALUES(?, GETDATE())");
        verify(ps).setString(1, "7:e27bf9c0c2313160ef960a15d44ced47");
        verify(ps, never()).setNull(eq(2), anyInt());
    }

    @Test
    public void testApplySqlVisitors() throws Exception {
        // given
        ReplaceSqlVisitor visitor = new ReplaceSqlVisitor();
        visitor.setReplace("\"KEY\"");
        visitor.setWith("\"key\"");
        sqlVisitors.add(visitor);
        database = new H2Database();
        InsertExecutablePreparedStatement statement = new InsertExecutablePreparedStatement(
                database,
                null,
                null,
                "names",
                new ArrayList<>(Arrays.asList(
                        new ColumnConfig()
                                .setName("key")
                                .setValueNumeric(1),
                        new ColumnConfig()
                                .setName("first")
                                .setValue("john"),
                        new ColumnConfig()
                                .setName("last")
                                .setValue("doe"))),
                changeSet,
                resourceAccessor);

        // when
        statement.execute(preparedStatementFactory);

        // then
        verify(connection).prepareStatement("INSERT INTO names(\"key\", first, last) VALUES(?, ?, ?)");
    }
}
