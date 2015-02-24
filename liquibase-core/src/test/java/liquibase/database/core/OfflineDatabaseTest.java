package liquibase.database.core;

import org.junit.Assert;
import org.junit.Test;

import liquibase.change.AddColumnConfig;
import liquibase.change.core.AddColumnChange;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.OfflineConnection;
import liquibase.exception.DatabaseException;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddColumnStatement;

public class OfflineDatabaseTest {

	private Database createOfflineDatabase(String url) throws DatabaseException {
		DatabaseConnection databaseConnection = new OfflineConnection(url);
		return DatabaseFactory.getInstance().openDatabase(url, null, null, null, null);
	}

	/**
	 * Check if it it's possible to output SQL from an OfflineConnection
	 * set to Oracle (offline:oracle).
	 *
	 * @see <a href="https://liquibase.jira.com/browse/CORE-2192">CORE-2192</a>
	 */
	@Test
	public void canOutputSQLFromOfflineOracleDB() {
		AddColumnChange change = new AddColumnChange();
		AddColumnConfig column1 = new AddColumnConfig();
		column1.setName("column1");
		column1.setType("INT");
		change.addColumn(column1);
		AddColumnConfig column2 = new AddColumnConfig();
		column2.setName("column2");
		column2.setType("INT");
		change.addColumn(column2);

		SqlStatement[] statements = new SqlStatement[0];
		try {
			statements = change.generateStatements(createOfflineDatabase("offline:oracle"));
		} catch (DatabaseException e) {
			Assert.fail("Can't generate statements from an Offline Oracle database.");
		}
		Assert.assertEquals(1, statements.length);
		Assert.assertTrue(statements[0] instanceof AddColumnStatement);
		AddColumnStatement stmt = (AddColumnStatement) statements[0];
		Assert.assertTrue(stmt.isMultiple());
		Assert.assertEquals(2, stmt.getColumns().size());
	}
}
