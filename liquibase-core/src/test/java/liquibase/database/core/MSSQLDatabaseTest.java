package liquibase.database.core;

import liquibase.database.AbstractJdbcDatabaseTest;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.QueryResult;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.easymock.classextension.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Tests for {@link MSSQLDatabase}
 */
public class MSSQLDatabaseTest extends AbstractJdbcDatabaseTest {

    public MSSQLDatabaseTest() throws Exception {
        super(new MSSQLDatabase());
    }

    @Override
    protected String getProductNameString() {
        return "Microsoft SQL Server";
    }


    @Override
    @Test
    public void supportsInitiallyDeferrableColumns() {
        assertFalse(getDatabase().supportsInitiallyDeferrableColumns());
    }

    @Override
    @Test
    public void getCurrentDateTimeFunction() {
        assertEquals("GETDATE()", getDatabase().getCurrentDateTimeFunction());
    }

    @Test
    public void getDefaultDriver() {
        Database database = new MSSQLDatabase();

        assertEquals("com.microsoft.sqlserver.jdbc.SQLServerDriver", database.getDefaultDriver("jdbc:sqlserver://localhost;databaseName=liquibase"));

        assertNull(database.getDefaultDriver("jdbc:oracle:thin://localhost;databaseName=liquibase"));
    }

    @Override
    @Test
    public void escapeTableName_noSchema() {
        Database database = new MSSQLDatabase();
        assertEquals("[tableName]", database.escapeTableName(null, null, "tableName"));
    }

    @Override
    @Test
    public void escapeTableName_withSchema() {
        Database database = new MSSQLDatabase();
        assertEquals("[schemaName].[tableName]", database.escapeTableName("catalogName", "schemaName", "tableName"));
    }

    @Test
    public void changeDefaultSchema() throws DatabaseException {
        Database database = new MSSQLDatabase();
        assertNull(database.getDefaultSchemaName());

        database.setDefaultSchemaName("myschema");
        assertEquals("myschema", database.getDefaultSchemaName());
    }
    
    private Database getADatabaseWithCollation( final String collation ) throws DatabaseException, SQLException {
		Database database = getDatabase();
		JdbcConnection connection = createMock(JdbcConnection.class);
		expect(connection.getConnectionUserName()).andReturn("user").anyTimes();
		expect(connection.getURL()).andReturn("URL").anyTimes();
		expect(connection.getAutoCommit()).andReturn(getDatabase().getAutoCommitMode()).anyTimes();

		Connection sqlConnection = createMock(Connection.class);

		expect(connection.getUnderlyingConnection()).andReturn(sqlConnection).anyTimes();
		expect( connection.query("SELECT CONVERT(varchar(100), SERVERPROPERTY('COLLATION'))")).andReturn(new QueryResult(collation));

		connection.attached(database);
		replay(connection, sqlConnection);
		database.setConnection(connection);
		return database;
    }
    
    @Test
    public void caseSensitiveBinaryCollation() throws Exception {
    	Database database =  getADatabaseWithCollation("Latin1_General_BIN");
    	assertTrue( "Should be case sensitive", database.isCaseSensitive() );
    }

    @Test
    public void caseSensitiveCICollation() throws Exception {
    	Database database = getADatabaseWithCollation("Latin1_General_CI_AI");
    	assertFalse( "Should be case insensitive", database.isCaseSensitive() );
    }
    @Test
    public void caseSensitiveCSCollation() throws Exception {
    	Database database =getADatabaseWithCollation("Latin1_General_CS_AI");    	
    	assertTrue( "Should be case sensitive", database.isCaseSensitive() );
    }
}
