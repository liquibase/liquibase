package liquibase.database.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import liquibase.change.CheckSum;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RanChangeSet;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogFactory;

/**
 * Cassandra 1.2.0 NoSQL database support.
 */
public class CassandraDatabase extends AbstractJdbcDatabase {
	public static final String PRODUCT_NAME = "Cassandra";

	@Override
	public boolean hasDatabaseChangeLogLockTable() throws DatabaseException {
		boolean hasChangeLogLockTable;
		try {
			Statement statement = getStatement();
			statement.executeQuery("select ID from DATABASECHANGELOGLOCK");
			statement.close();
			hasChangeLogLockTable = true;
		} catch (SQLException e) {
			LogFactory.getLogger().info("No DATABASECHANGELOGLOCK available in cassandra.");
			hasChangeLogLockTable = false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			hasChangeLogLockTable = false;
		}

		// needs to be generated up front
		return hasChangeLogLockTable;
	}

	/**
	 * This method will check the database ChangeLogLock table used to keep track of if a machine is updating the database. If the table does not exist it will
	 * create one otherwise it will not do anything besides outputting a log message.
	 */
	public void checkDatabaseChangeLogLockTable() throws DatabaseException {
		if (!hasDatabaseChangeLogLockTable()) {
			try {
				Statement statement = getStatement();
				statement.executeUpdate("CREATE TABLE DATABASECHANGELOGLOCK (ID int PRIMARY KEY, LOCKED boolean, LOCKGRANTED timestamp, LOCKEDBY text)");
				statement.close();

				statement = getStatement();
				statement.executeUpdate("insert into DATABASECHANGELOGLOCK (ID, LOCKED) values (1, false)");
				statement.close();
			} catch (SQLException e) {
				LogFactory.getLogger().info("No DATABASECHANGELOG available in cassandra.");
				throw new RuntimeException(e);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public boolean hasDatabaseChangeLogTable() throws DatabaseException {
		boolean hasChangeLogTable;
		try {
			Statement statement = getStatement();
			statement.executeQuery("select ID from DATABASECHANGELOG");
			statement.close();
			hasChangeLogTable = true;
		} catch (SQLException e) {
			LogFactory.getLogger().info("No DATABASECHANGELOG available in cassandra.");
			hasChangeLogTable = false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			hasChangeLogTable = false;
		}

		// needs to be generated up front
		return hasChangeLogTable;
	}

	/**
	 * This method will check the database ChangeLog table used to keep track of the changes in the file. If the table does not exist it will create one
	 * otherwise it will not do anything besides outputting a log message.
	 * 
	 * @param updateExistingNullChecksums
	 * @param contexts
	 */
	@Override
	public void checkDatabaseChangeLogTable(boolean updateExistingNullChecksums, DatabaseChangeLog databaseChangeLog, String... contexts)
			throws DatabaseException {
		if (!hasDatabaseChangeLogTable()) {
			try {
				Statement statement = getStatement();
				statement
						.executeUpdate("CREATE TABLE DATABASECHANGELOG (ID text PRIMARY KEY, AUTHOR text, FILENAME text, DATEEXECUTED timestamp, ORDEREXECUTED int, EXECTYPE text, MD5SUM text, DESCRIPTION text, COMMENTS text, TAG text, LIQUIBASE text)");
				statement.close();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public String getShortName() {
		return "cassandra";
	}

	public CassandraDatabase() {
		setDefaultSchemaName("");
	}

	public int getPriority() {
		return PRIORITY_DEFAULT;
	}

	@Override
	protected String getDefaultDatabaseProductName() {
		return "Cassandra";
	}

	public Integer getDefaultPort() {
		return 9160;
	}

	public boolean supportsInitiallyDeferrableColumns() {
		return false;
	}

	@Override
	public boolean supportsSequences() {
		return false;
	}

	public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
		String databaseProductName = conn.getDatabaseProductName();
		return PRODUCT_NAME.equalsIgnoreCase(databaseProductName);
	}

	public String getDefaultDriver(String url) {
		return "org.apache.cassandra.cql.jdbc.CassandraDriver";
	}

	public boolean supportsTablespaces() {
		return false;
	}

	@Override
	public boolean supportsRestrictForeignKeys() {
		return false;
	}

	@Override
	public boolean supportsDropTableCascadeConstraints() {
		return false;
	}

	@Override
	public boolean isAutoCommit() throws DatabaseException {
		return true;
	}

	@Override
	public void setAutoCommit(boolean b) throws DatabaseException {
	}

	@Override
	public boolean isCaseSensitive() {
		return true;
	}

	@Override
	public int getNextChangeSetSequenceValue() throws LiquibaseException {
		int next = 0;
		try {
			Statement statement = getStatement();
			ResultSet rs = statement.executeQuery("SELECT KEY, AUTHOR, ORDEREXECUTED FROM DATABASECHANGELOGLOCK");
			while (rs.next()) {
				int order = rs.getInt("ORDEREXECUTED");
				next = Math.max(order, next);
			}
			statement.close();

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return next + 1;
	}

	protected Statement getStatement() throws ClassNotFoundException, SQLException {
		String url = super.getConnection().getURL();
		Class.forName("org.apache.cassandra.cql.jdbc.CassandraDriver");
		Connection con = DriverManager.getConnection(url);
		Statement statement = con.createStatement();
		return statement;
	}

	@Override
	public List<RanChangeSet> getRanChangeSetList() throws DatabaseException {
		List<RanChangeSet> ranChangeSetList = new ArrayList<RanChangeSet>();

		if (hasDatabaseChangeLogTable()) {
			try {
				Statement statement = getStatement();
				ResultSet rs = statement
						.executeQuery("SELECT AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED, TAG FROM DATABASECHANGELOG");

				while (rs.next()) {
					String fileName = rs.getString("FILENAME");
					String author = rs.getString("AUTHOR");
					String id = rs.getString("ID");
					String md5sum = rs.getString("MD5SUM") == null ? null : rs.getString("MD5SUM");
					String description = rs.getString("DESCRIPTION") == null ? null : rs.getString("DESCRIPTION");
					Object tmpDateExecuted = rs.getString("DATEEXECUTED");
					Date dateExecuted = null;
					if (tmpDateExecuted instanceof Date) {
						dateExecuted = (Date) tmpDateExecuted;
					} else {
						DateFormat df = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");
						try {
							dateExecuted = df.parse((String) tmpDateExecuted);
						} catch (Exception e) {
							LogFactory.getLogger().warning("Failed to parse date: " + tmpDateExecuted + " expected " + df.format(new Date(0)));
							dateExecuted = null;
						}
					}
					String tag = rs.getString("TAG") == null ? null : rs.getString("TAG");
					String execType = rs.getString("EXECTYPE") == null ? null : rs.getString("EXECTYPE");
					try {
						RanChangeSet ranChangeSet = new RanChangeSet(fileName, id, author, CheckSum.parse(md5sum), dateExecuted, tag,
								ChangeSet.ExecType.valueOf(execType), description);
						LogFactory.getLogger().debug("Changeset already ran on cassandra Cassandra: " + ranChangeSet);
						ranChangeSetList.add(ranChangeSet);
					} catch (IllegalArgumentException e) {
						LogFactory.getLogger().severe("Unknown EXECTYPE from database: " + execType);
						throw e;
					}
				}
				statement.close();
			} catch (Exception e) {
				throw new UnexpectedLiquibaseException(e);
			}
		}

		LogFactory.getLogger().debug("Changesets ran: "+ranChangeSetList.size());
		return ranChangeSetList;
	}

	public String getCurrentDateTimeFunction() {
		// no alternative in cassandra, using client time
		return String.valueOf(System.currentTimeMillis());
	}

}
