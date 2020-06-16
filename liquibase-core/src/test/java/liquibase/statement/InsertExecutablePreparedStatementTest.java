package liquibase.statement;

import liquibase.change.ColumnConfig;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.PreparedStatementFactory;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class InsertExecutablePreparedStatementTest {

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
        database = new MSSQLDatabase();
        preparedStatementFactory = new PreparedStatementFactory(connection);

        given(connection.prepareStatement(anyString())).willReturn(ps);
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
        verify(ps).close();
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
        verify(ps).close();
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
                "INSERT INTO catalogName.schemaName.tableName(column2, column3, column4) VALUES(?, (select * from abc where x=y), ?)");
        verify(ps).setString(1, "value2");
        verify(ps).setString(2, "value4");
        verify(ps).execute();
        verify(ps).close();
        verifyNoMoreInteractions(ps);
    }
}
