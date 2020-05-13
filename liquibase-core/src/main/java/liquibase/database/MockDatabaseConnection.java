package liquibase.database;

import liquibase.exception.DatabaseException;

public class MockDatabaseConnection implements DatabaseConnection {

	private int databaseMajorVersion = 999;
	private int databaseMinorVersion = 999;

	@Override
	public void close() throws DatabaseException {
	}

	@Override
	public void commit() throws DatabaseException {
	}

	@Override
	public boolean getAutoCommit() throws DatabaseException {
		return false;
	}

	@Override
	public String getCatalog() throws DatabaseException {
		return null;
	}

	@Override
	public String nativeSQL(String sql) throws DatabaseException {
		return sql;
	}

	@Override
	public void rollback() throws DatabaseException {
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws DatabaseException {
	}

	@Override
	public String getDatabaseProductName() throws DatabaseException
	{
		return null;
	}

	@Override
	public String getDatabaseProductVersion() throws DatabaseException {
		return null;
	}

	@Override
	public int getDatabaseMajorVersion() throws DatabaseException {
		return databaseMajorVersion;
	}

	public MockDatabaseConnection setDatabaseMajorVersion(int databaseMajorVersion) {
		this.databaseMajorVersion = databaseMajorVersion;
		return this;
	}

	@Override
	public int getDatabaseMinorVersion() throws DatabaseException {
		return databaseMinorVersion;
	}

	public MockDatabaseConnection setDatabaseMinorVersion(int databaseMinorVersion) {
		this.databaseMinorVersion = databaseMinorVersion;
		return this;
	}

	@Override
	public String getURL() {
		return null;
	}

	@Override
	public String getConnectionUserName() {
		return null;
	}

	@Override
	public boolean isClosed() throws DatabaseException {
		return false;
	}

	@Override
	public void attached(Database database) {

	}
}
