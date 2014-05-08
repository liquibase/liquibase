package liquibase.database.core;

import static org.easymock.classextension.EasyMock.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import liquibase.database.AbstractJdbcDatabaseTest;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;

import static org.junit.Assert.*;
import org.junit.Test;

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
		Statement statement = createMock(Statement.class);
		ResultSet resultSet = createMock(ResultSet.class);
		ResultSetMetaData metadata = createMock(ResultSetMetaData.class);

		expect(connection.getUnderlyingConnection()).andReturn(sqlConnection).anyTimes();
		expect( sqlConnection.createStatement()).andReturn(statement);
		expect( statement.executeQuery("SELECT CONVERT(varchar(100), SERVERPROPERTY('COLLATION'))")).andReturn(resultSet);
		expect( resultSet.next() ).andReturn(true);
		expect( resultSet.getMetaData() ).andReturn(metadata);
		expect( metadata.getColumnCount() ).andReturn(1);
		expect( resultSet.getString(1)).andReturn(collation);
		expect( resultSet.next() ).andReturn(false);

		connection.attached(database);
		replay(connection, sqlConnection, statement, resultSet, metadata);
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
