package liquibase.snapshot.core;

import liquibase.database.Database;
import liquibase.database.JdbcConnection;
import liquibase.database.core.OracleDatabase;
import liquibase.database.structure.*;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.util.JdbcUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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

	@Override
    public List<ForeignKey> getAdditionalForeignKeys(String schemaName, Database database) throws DatabaseException {
		List<ForeignKey> foreignKeys = super.getAdditionalForeignKeys(schemaName, database);

		// Create SQL statement to select all FKs in database which referenced to unique columns
		String query = "select uc_fk.constraint_name FK_NAME,uc_fk.owner FKTABLE_SCHEM,ucc_fk.table_name FKTABLE_NAME,ucc_fk.column_name FKCOLUMN_NAME,decode(uc_fk.deferrable, 'DEFERRABLE', 5 ,'NOT DEFERRABLE', 7 , 'DEFERRED', 6 ) DEFERRABILITY, decode(uc_fk.delete_rule, 'CASCADE', 0,'NO ACTION', 3) DELETE_RULE,ucc_rf.table_name PKTABLE_NAME,ucc_rf.column_name PKCOLUMN_NAME from user_cons_columns ucc_fk,user_constraints uc_fk,user_cons_columns ucc_rf,user_constraints uc_rf where uc_fk.CONSTRAINT_NAME = ucc_fk.CONSTRAINT_NAME and uc_fk.constraint_type='R' and uc_fk.r_constraint_name=ucc_rf.CONSTRAINT_NAME and uc_rf.constraint_name = ucc_rf.constraint_name and uc_rf.constraint_type = 'U'";
		try {
			Statement statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
			ResultSet rs = statement.executeQuery(query);
			while (rs.next()) {
				ForeignKeyInfo fkInfo = new ForeignKeyInfo();
				fkInfo.setReferencesUniqueColumn(true);
				fkInfo.setFkName(convertFromDatabaseName(rs.getString("FK_NAME")));
				fkInfo.setFkSchema(convertFromDatabaseName(rs.getString("FKTABLE_SCHEM")));
				fkInfo.setFkTableName(convertFromDatabaseName(rs.getString("FKTABLE_NAME")));
				fkInfo.setFkColumn(convertFromDatabaseName(rs.getString("FKCOLUMN_NAME")));

				fkInfo.setPkTableName(convertFromDatabaseName(rs.getString("PKTABLE_NAME")));
				fkInfo.setPkColumn(convertFromDatabaseName(rs.getString("PKCOLUMN_NAME")));

				fkInfo.setDeferrablility(rs.getShort("DEFERRABILITY"));
				ForeignKeyConstraintType deleteRule = convertToForeignKeyConstraintType(rs.getInt("DELETE_RULE"));
	            if (rs.wasNull()) {
		            deleteRule = null;
	            }
				fkInfo.setDeleteRule(deleteRule);
				foreignKeys.add(generateForeignKey(fkInfo, database, foreignKeys));
			}
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeStatement(statement);
		} catch (SQLException e) {
			throw new DatabaseException("Can't execute selection query to generate list of foreign keys", e);
		}
		return foreignKeys;
	}

	protected void readIndexes(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws DatabaseException, SQLException {
		Database database = snapshot.getDatabase();
		updateListeners("Reading indexes for " + database.toString() + " ...");

		String query = "select aic.index_name, 3 AS TYPE, aic.table_name, aic.column_name, aic.column_position AS ORDINAL_POSITION, null AS FILTER_CONDITION, ai.tablespace_name AS TABLESPACE FROM all_ind_columns aic, all_indexes ai WHERE aic.table_owner='" + database.convertRequestedSchemaToSchema(schema) + "' and aic.index_name = ai.index_name ORDER BY INDEX_NAME, ORDINAL_POSITION";
		Statement statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
		ResultSet rs = statement.executeQuery(query);

		Map<String, Index> indexMap = new HashMap<String, Index>();
		while (rs.next()) {
			String indexName = convertFromDatabaseName(rs.getString("INDEX_NAME"));
			String tableName = rs.getString("TABLE_NAME");
			String tableSpace = rs.getString("TABLESPACE");
			String columnName = convertFromDatabaseName(rs.getString("COLUMN_NAME"));
			if (columnName == null) {
				//nothing to index, not sure why these come through sometimes
				continue;
			}
			short type = rs.getShort("TYPE");
			boolean nonUnique = true;
			try {
				nonUnique = rs.getBoolean("NON_UNIQUE");
			} catch (SQLException e) {
				//doesn't exist in all databases
			}

			short position = rs.getShort("ORDINAL_POSITION");
			String filterCondition = rs.getString("FILTER_CONDITION");

			if (type == DatabaseMetaData.tableIndexStatistic) {
				continue;
			}

			Index index;
			if (indexMap.containsKey(indexName)) {
				index = indexMap.get(indexName);
			} else {
				index = new Index();
				index.setTable(snapshot.getTable(tableName));
				index.setTablespace(tableSpace);
				index.setName(indexName);
				index.setUnique(!nonUnique);
				index.setFilterCondition(filterCondition);
				indexMap.put(indexName, index);
			}

			for (int i = index.getColumns().size(); i < position; i++) {
				index.getColumns().add(null);
			}
			index.getColumns().set(position - 1, columnName);
		}
		JdbcUtils.closeResultSet(rs);
		JdbcUtils.closeStatement(statement);

		for (Map.Entry<String, Index> entry : indexMap.entrySet()) {
			snapshot.getIndexes().add(entry.getValue());
		}

		/*
		* marks indexes as "associated with" instead of "remove it"
		* Index should have associations with:
		* foreignKey, primaryKey or uniqueConstraint
		* */
		for (Index index : snapshot.getIndexes()) {
			for (PrimaryKey pk : snapshot.getPrimaryKeys()) {
				if (index.getTable().getName().equalsIgnoreCase(pk.getTable().getName()) && index.getColumnNames().equals(pk.getColumnNames())) {
					index.addAssociatedWith(Index.MARK_PRIMARY_KEY);
				}
			}
			for (ForeignKey fk : snapshot.getForeignKeys()) {
				if (index.getTable().getName().equalsIgnoreCase(fk.getForeignKeyTable().getName()) && index.getColumnNames().equals(fk.getForeignKeyColumns())) {
					index.addAssociatedWith(Index.MARK_FOREIGN_KEY);
				}
			}
			for (UniqueConstraint uc : snapshot.getUniqueConstraints()) {
				if (index.getTable().getName().equalsIgnoreCase(uc.getTable().getName()) && index.getColumnNames().equals(uc.getColumnNames())) {
					index.addAssociatedWith(Index.MARK_UNIQUE_CONSTRAINT);
				}
			}
		}
	}

	protected void readPrimaryKeys(DatabaseSnapshot snapshot, String schema, DatabaseMetaData databaseMetaData) throws DatabaseException, SQLException {
		Database database = snapshot.getDatabase();
		updateListeners("Reading primary keys for " + database.toString() + " ...");

		//we can't add directly to the this.primaryKeys hashSet because adding columns to an exising PK changes the hashCode and .contains() fails
		List<PrimaryKey> foundPKs = new ArrayList<PrimaryKey>();
		String query = "select uc.table_name TABLE_NAME,ucc.column_name COLUMN_NAME,ucc.position KEY_SEQ,uc.constraint_name PK_NAME,ui.tablespace_name TABLESPACE from user_constraints uc,user_indexes ui,user_cons_columns ucc where uc.constraint_type = 'P' and uc.index_name = ui.index_name and uc.constraint_name = ucc.constraint_name";
		Statement statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
		ResultSet rs = statement.executeQuery(query);

		while (rs.next()) {
			String tableName = convertFromDatabaseName(rs.getString("TABLE_NAME"));
			String tablespace = convertFromDatabaseName(rs.getString("TABLESPACE"));
			String columnName = convertFromDatabaseName(rs.getString("COLUMN_NAME"));
			short position = rs.getShort("KEY_SEQ");

			boolean foundExistingPK = false;
			for (PrimaryKey pk : foundPKs) {
				if (pk.getTable().getName().equals(tableName)) {
					pk.addColumnName(position - 1, columnName);

					foundExistingPK = true;
				}
			}

			if (!foundExistingPK) {
				PrimaryKey primaryKey = new PrimaryKey();
				primaryKey.setTablespace(tablespace);
				primaryKey.setTable(snapshot.getTable(tableName));
				primaryKey.addColumnName(position - 1, columnName);
				primaryKey.setName(convertPrimaryKeyName(rs.getString("PK_NAME")));

				foundPKs.add(primaryKey);
			}
		}
		JdbcUtils.closeResultSet(rs);
		JdbcUtils.closeStatement(statement);

		snapshot.getPrimaryKeys().addAll(foundPKs);
	}
}