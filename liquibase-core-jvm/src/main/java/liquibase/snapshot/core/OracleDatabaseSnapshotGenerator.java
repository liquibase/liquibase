package liquibase.snapshot.core;

import liquibase.database.Database;
import liquibase.database.JdbcConnection;
import liquibase.database.core.OracleDatabase;
import liquibase.database.structure.Column;
import liquibase.database.structure.Table;
import liquibase.database.structure.UniqueConstraint;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OracleDatabaseSnapshotGenerator extends JdbcDatabaseSnapshotGenerator {
	public boolean supports(Database database) {
		return database instanceof OracleDatabase;
	}

	public int getPriority(Database database) {
		return PRIORITY_DATABASE;
	}

	@Override
	protected String convertTableNameToDatabaseTableName(String tableName) {
		return tableName.toUpperCase();
	}

	@Override
	protected String convertColumnNameToDatabaseTableName(String columnName) {
		return columnName.toUpperCase();
	}

	/** Oracle specific implementation */
	@Override
	protected void getColumnTypeAndDefValue(Column columnInfo, ResultSet rs, Database database) throws SQLException, DatabaseException {
		super.getColumnTypeAndDefValue(columnInfo, rs, database);

		String columnTypeName = rs.getString("TYPE_NAME");
		if ("VARCHAR2".equals(columnTypeName)) {
			int charOctetLength = rs.getInt("CHAR_OCTET_LENGTH");
			int columnSize = rs.getInt("COLUMN_SIZE");
			if (columnSize == charOctetLength) {
				columnInfo.setLengthSemantics(Column.LengthSemantics.BYTE);
			} else {
				columnInfo.setLengthSemantics(Column.LengthSemantics.CHAR);
			}
		}
	}

	@Override
	protected void readUniqueConstraints(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws DatabaseException, SQLException {
		Database database = snapshot.getDatabase();
		updateListeners("Reading unique constraints for " + database.toString() + " ...");
		List<UniqueConstraint> foundUC = new ArrayList<UniqueConstraint>();

		Connection jdbcConnection = ((JdbcConnection) database.getConnection()).getUnderlyingConnection();

		PreparedStatement statement = null;
		ResultSet rs = null;

		// Setting default schema name. Needed for correct statement generation
		if (schema == null)
			schema = database.convertRequestedSchemaToSchema(schema);

		try {
			statement = jdbcConnection.prepareStatement("select constraint_name, table_name, status, deferrable, deferred "
			                                            + "from all_constraints where constraint_type='U' and owner='" + schema + "'");
			rs = statement.executeQuery();
			while (rs.next()) {
				String constraintName = rs.getString("constraint_name");
				String tableName = rs.getString("table_name");
				String status = rs.getString("status");
				String deferrable = rs.getString("deferrable");
				String deferred = rs.getString("deferred");
				UniqueConstraint constraintInformation = new UniqueConstraint();
				constraintInformation.setName(constraintName);
				if (!database.isSystemTable(null, schema, tableName) && !database.isLiquibaseTable(tableName)) {
					Table table = snapshot.getTable(tableName);
					if (table == null) {
						throw new IllegalStateException("Cannot find table for " + tableName);
					}
					constraintInformation.setTable(table);
					constraintInformation.setDisabled("DISABLED".equals(status));
					if ("DEFERRABLE".equals(deferrable)) {
						constraintInformation.setDeferrable(true);
						constraintInformation.setInitiallyDeferred("DEFERRED".equals(deferred));
					}
					getColumnsForUniqueConstraint(jdbcConnection, constraintInformation);
					foundUC.add(constraintInformation);
				}
			}
			snapshot.getUniqueConstraints().addAll(foundUC);
		} finally {
			rs.close();
			if (statement != null) {
				statement.close();
			}

		}
	}

	protected void getColumnsForUniqueConstraint(Connection jdbcConnection, UniqueConstraint constraint) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = jdbcConnection.prepareStatement("select column_name from user_cons_columns where constraint_name=? order by position");
			stmt.setString(1, constraint.getName());
			rs = stmt.executeQuery();
			while (rs.next()) {
				String columnName = rs.getString("column_name");
				constraint.getColumns().add(columnName);
			}
		} finally {
			rs.close();
			if (stmt != null)
				stmt.close();
		}
	}
}
