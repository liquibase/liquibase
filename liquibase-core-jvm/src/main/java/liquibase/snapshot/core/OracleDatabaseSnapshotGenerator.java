package liquibase.snapshot.core;

import liquibase.database.Database;
import liquibase.database.JdbcConnection;
import liquibase.database.core.OracleDatabase;
import liquibase.database.structure.Column;
import liquibase.database.structure.Table;
import liquibase.database.structure.UniqueConstraint;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.util.JdbcUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OracleDatabaseSnapshotGenerator extends JdbcDatabaseSnapshotGenerator {

	private List<String> integerList = new ArrayList<String>();

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

		// Exclusive setting for oracle INTEGER type
		// Details:
		// INTEGER means NUMBER type with 'data_precision IS NULL and scale = 0' 
		if (columnInfo.getDataType() == Types.INTEGER) {
			columnInfo.setTypeName("INTEGER");
		}

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

	protected void readColumns(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws SQLException, DatabaseException {
		findIntegerColumns(snapshot);
		super.readColumns(snapshot, schema, databaseMetaData);
	}

	/**
	*  Method finds all INTEGER columns in snapshot's database
	*
	 * @param snapshot current database snapshot
	 * @throws java.sql.SQLException execute statement error
	 * @return String list with names of all INTEGER columns
	 * */
	private List<String> findIntegerColumns(DatabaseSnapshot snapshot) throws SQLException {
		Database database = snapshot.getDatabase();
		Statement statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();

		// Finding all columns created as 'INTEGER'
		ResultSet integerListRS = statement.executeQuery("select * from user_tab_columns where data_precision is null and data_scale = 0 and data_type = 'NUMBER'");
		while (integerListRS.next()) {
			integerList.add(integerListRS.getString("TABLE_NAME") + "." + integerListRS.getString("COLUMN_NAME"));
		}

		JdbcUtils.closeResultSet(integerListRS);
		JdbcUtils.closeStatement(statement);

		return integerList;
	}

	protected void configureColumnType(Column column, ResultSet rs) throws SQLException {
		if (integerList.contains(column.getTable().getName() + "." + column.getName())) {
		    column.setDataType(Types.INTEGER);
	    } else {
		    column.setDataType(rs.getInt("DATA_TYPE"));
	    }
		column.setColumnSize(rs.getInt("COLUMN_SIZE"));
		column.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));

		// Set true, if precision should be initialize
		column.setInitPrecision(
				!((column.getDataType() == Types.DECIMAL ||
				   column.getDataType() == Types.NUMERIC ||
				   column.getDataType() == Types.REAL) && rs.getString("DECIMAL_DIGITS") == null)
		);
	}
}