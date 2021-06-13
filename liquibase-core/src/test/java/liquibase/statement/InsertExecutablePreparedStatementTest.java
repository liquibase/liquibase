package liquibase.statement;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import liquibase.change.ColumnConfig;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.PreparedStatementFactory;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ResourceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

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
        initMocks(this);
        when(connection.prepareStatement(any(String.class))).thenReturn(ps);
        database = new MSSQLDatabase();
        preparedStatementFactory = new PreparedStatementFactory(connection);
    }

	@Test
	public void testExecuteWithClobAndComputedValue() throws Exception {
        // given
        InsertExecutablePreparedStatement statement = new InsertExecutablePreparedStatement(
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
        verify(connection).prepareStatement("INSERT INTO DATABASECHANGELOG(MD5SUM, DATEEXECUTED) VALUES(?, GETDATE())");
		verify(ps).setString(1, "7:e27bf9c0c2313160ef960a15d44ced47");
		verify(ps, never()).setNull(eq(2), anyInt());
	}

}
