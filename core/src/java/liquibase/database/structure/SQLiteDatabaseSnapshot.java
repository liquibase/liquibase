package liquibase.database.structure;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import liquibase.database.AbstractDatabase;
import liquibase.database.Database;
import liquibase.database.SQLiteDatabase;
import liquibase.database.sql.visitor.SqlVisitor;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.JDBCException;
import liquibase.util.StringUtils;

public class SQLiteDatabaseSnapshot extends SqlDatabaseSnapshot {

	/**
     * Creates an empty database snapshot
     */
    public SQLiteDatabaseSnapshot() {
    }

    /**
     * Creates a snapshot of the given database with no status listeners
     */
    public SQLiteDatabaseSnapshot(Database database) throws JDBCException {
        this(database, null, null);
    }

    /**
     * Creates a snapshot of the given database with no status listeners
     */
    public SQLiteDatabaseSnapshot(Database database, String schema) 
    		throws JDBCException {
        this(database, null, schema);
    }

    /**
     * Creates a snapshot of the given database.
     */
    public SQLiteDatabaseSnapshot(Database database, 
    		Set<DiffStatusListener> statusListeners) throws JDBCException {
        this(database, statusListeners, database.getDefaultSchemaName());
    }

    /**
     * Creates a snapshot of the given database.
     */
    public SQLiteDatabaseSnapshot(Database database, 
    		Set<DiffStatusListener> statusListeners, String requestedSchema) 
    		throws JDBCException {
    	super(database, statusListeners, requestedSchema);
    }
	
    /**
     * SQLite specific implementation
     */	
	@Override
	protected void readTablesAndViews(String schema) throws SQLException, 
			JDBCException {
        
		updateListeners("Reading tables for " + database.toString() + " ...");
        ResultSet rs = databaseMetaData.getTables(
        		database.convertRequestedSchemaToCatalog(schema), 
        		database.convertRequestedSchemaToSchema(schema), 
        		null, 
        		new String[]{"TABLE", "VIEW"});
        
        while (rs.next()) {
            String type = rs.getString("TABLE_TYPE");
            String name = rs.getString("TABLE_NAME");
            String schemaName = rs.getString("TABLE_SCHEM");
            String catalogName = rs.getString("TABLE_CAT");
            String remarks = rs.getString("REMARKS");

            if (database.isSystemTable(catalogName, schemaName, name) || 
            		database.isLiquibaseTable(name) || 
            		database.isSystemView(catalogName, schemaName, name)) {
                continue;
            }

            if ("TABLE".equals(type)) {
                Table table = new Table(name);
                table.setRemarks(StringUtils.trimToNull(remarks));
                table.setDatabase(database);
                table.setSchema(schemaName);
                tablesMap.put(name, table);
            } else if ("VIEW".equals(type)) {
                View view = new View();
                view.setName(name);
                view.setSchema(schemaName);
                try {
                    view.setDefinition(database.
                    		getViewDefinition(schema, name));
                } catch (JDBCException e) {
                    System.out.println("Error getting view with " + 
                    	((AbstractDatabase)database).
                    		getViewDefinitionSql(schema, name));
                    throw e;
                }
                viewsMap.put(name, view);
            }
        }
        rs.close();
	}
	
	/**
     * SQLite specific implementation
     */	
	@Override
	protected void readForeignKeyInformation(String schema) throws JDBCException, SQLException {
        updateListeners("Reading foreign keys for " + database.toString() + " ...");
        // Foreign keys are not supported in SQLite until now. 
        // ...do nothing here
    }
	
	/**
     * SQLite specific implementation
     */	
	@Override
	protected void readPrimaryKeys(String schema) throws JDBCException, SQLException {
        updateListeners("Reading primary keys for " + database.toString() + " ...");

        //we can't add directly to the this.primaryKeys hashSet because adding columns to an exising PK changes the hashCode and .contains() fails
        List<PrimaryKey> foundPKs = new ArrayList<PrimaryKey>();

        for (Table table : tablesMap.values()) {
            ResultSet rs = databaseMetaData.getPrimaryKeys(database.convertRequestedSchemaToCatalog(schema), database.convertRequestedSchemaToSchema(schema), table.getName());

            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                String columnName = rs.getString("COLUMN_NAME");
                short position = rs.getShort("KEY_SEQ");
                
                if (!(database instanceof SQLiteDatabase)) {
                	position -= 1;
                }

                boolean foundExistingPK = false;
                for (PrimaryKey pk : foundPKs) {
                    if (pk.getTable().getName().equals(tableName)) {
                        pk.addColumnName(position, columnName);

                        foundExistingPK = true;
                    }
                }

                if (!foundExistingPK) {
                    PrimaryKey primaryKey = new PrimaryKey();
                    primaryKey.setTable(table);
                    primaryKey.addColumnName(position, columnName);
                    primaryKey.setName(rs.getString("PK_NAME"));

                    foundPKs.add(primaryKey);
                }
            }

            rs.close();
        }

        this.primaryKeys.addAll(foundPKs);
    }
	
	protected void readColumns(String schema) throws SQLException, JDBCException {
        updateListeners("Reading columns for " + database.toString() + " ...");

        if (database instanceof SQLiteDatabase) {
        	// ...work around for SQLite
        	for (Table cur_table:tablesMap.values()) {
    	        Statement selectStatement = database.getConnection().createStatement();
    	        ResultSet rs = databaseMetaData.getColumns(database.convertRequestedSchemaToCatalog(schema), database.convertRequestedSchemaToSchema(schema), cur_table.getName(), null);
    	        if (rs==null) {
    	        	rs = databaseMetaData.getColumns(database.convertRequestedSchemaToCatalog(schema), database.convertRequestedSchemaToSchema(schema), cur_table.getName(), null);
    	        }
    	        while ((rs!=null) && rs.next()) {
    	        	Column columnInfo = readColumnInfo(schema,rs);
    	        	if (columnInfo!=null) {
    	        		columnsMap.put(columnInfo.getTable().getName() + "." + columnInfo.getName(), columnInfo);
    	        	}
    	        }
    	        if (rs!=null) {
    	        	rs.close();
    	        }
    	        selectStatement.close();
        	}
        } else {
        	// ...if it is no SQLite database
	        Statement selectStatement = database.getConnection().createStatement();
	        ResultSet rs = databaseMetaData.getColumns(database.convertRequestedSchemaToCatalog(schema), database.convertRequestedSchemaToSchema(schema), null, null);
	        while (rs.next()) {
	            Column columnInfo = readColumnInfo(schema,rs);
	            if (columnInfo!=null) {
	            	columnsMap.put(columnInfo.getTable().getName() + "." + columnInfo.getName(), columnInfo);
	            }
	        }
	        rs.close();
	        selectStatement.close();
        }
    }
	
	private Column readColumnInfo(String schema, ResultSet rs) throws SQLException, JDBCException {
    	Column columnInfo = new Column();    	
    	
        String tableName = rs.getString("TABLE_NAME");
        String columnName = rs.getString("COLUMN_NAME");
        String schemaName = rs.getString("TABLE_SCHEM");
        String catalogName = rs.getString("TABLE_CAT");
                
        String upperCaseTableName = tableName.toUpperCase(Locale.ENGLISH);
        
        if (database.isSystemTable(catalogName, schemaName, upperCaseTableName) || 
        		database.isLiquibaseTable(upperCaseTableName)) {
            return null;
        }

        Table table = tablesMap.get(tableName);
        if (table == null) {
            View view = viewsMap.get(tableName);
            if (view == null) {
                // Not a table or view column. It's probably an index or primary key column, so ignore it.
                return null;
            } else {
                columnInfo.setView(view);
                view.getColumns().add(columnInfo);
            }
        } else {
            columnInfo.setTable(table);
            table.getColumns().add(columnInfo);
        }

        columnInfo.setName(columnName);
        columnInfo.setDataType(rs.getInt("DATA_TYPE"));
        columnInfo.setColumnSize(rs.getInt("COLUMN_SIZE"));
        columnInfo.setDecimalDigits(rs.getInt("DECIMAL_POINTS"));
        Object defaultValue = rs.getObject("COLUMN_DEF");
        try {
            columnInfo.setDefaultValue(database.convertDatabaseValueToJavaObject(defaultValue, columnInfo.getDataType(), columnInfo.getColumnSize(), columnInfo.getDecimalDigits()));
        } catch (ParseException e) {
            throw new JDBCException(e);
        }

        int nullable = rs.getInt("NULLABLE");
        if (nullable == DatabaseMetaData.columnNoNulls) {
            columnInfo.setNullable(false);
        } else if (nullable == DatabaseMetaData.columnNullable) {
            columnInfo.setNullable(true);
        }

        columnInfo.setPrimaryKey(isPrimaryKey(columnInfo));
        columnInfo.setAutoIncrement(database.isColumnAutoIncrement(schema, tableName, columnName));
        columnInfo.setTypeName(database.getColumnType(rs.getString("TYPE_NAME"), columnInfo.isAutoIncrement()));            
            	
        return columnInfo;
    }
	
	protected void readIndexes(String schema) throws JDBCException, SQLException {
        updateListeners("Reading indexes for " + database.toString() + " ...");

        for (Table table : tablesMap.values()) {
            ResultSet rs = null;
            Statement statement = null;
            Map<String, Index> indexMap = new HashMap<String, Index>();            
           
        	// for the odbc driver at http://www.ch-werner.de/sqliteodbc/ 
        	// databaseMetaData.getIndexInfo is not implemented
        	statement = database.getConnection().createStatement();
        	String sql = "PRAGMA index_list("+table.getName()+");";
        	try {
        		rs = statement.executeQuery(sql);
        	} catch(SQLException e) {
        		if (!e.getMessage().equals("query does not return ResultSet")) {
        			System.err.println(e);
//            			throw e;
        		}            		
        	}
        	while ((rs!=null) && rs.next()) {
        		String index_name = rs.getString("name");
        		boolean index_unique = rs.getBoolean("unique");
        		sql = "PRAGMA index_info("+index_name+");";
				Statement statement_2 = database.getConnection().createStatement();
				ResultSet rs_2 = statement_2.executeQuery(sql);
            	while ((rs_2!=null) && rs_2.next()) {
            		int index_column_seqno = rs_2.getInt("seqno");
//                		int index_column_cid = rs.getInt("cid");
            		String index_column_name = rs_2.getString("name");
            		if (index_unique) {
            			Column column = columnsMap.get(
            					table.getName()+"."+index_column_name);
            			column.setUnique(true);
            		} else {
                		Index indexInformation;
    	                if (indexMap.containsKey(index_name)) {
    	                    indexInformation = indexMap.get(index_name);
    	                } else {
    	                    indexInformation = new Index();
    	                    indexInformation.setTable(table);
    	                    indexInformation.setName(index_name);
    	                    indexInformation.setFilterCondition("");
    	                    indexMap.put(index_name, indexInformation);
    	                }
    	                indexInformation.getColumns().add(index_column_seqno,index_column_name);
            		}
            	}
            	if (rs_2!=null) {
            		rs_2.close();
            	}
            	if (statement_2 != null) {
	                statement_2.close();
	            }
            	
        	}
        	if (rs!=null) {
        		rs.close();
        	}
            if (statement != null) {
                statement.close();
            }
    
            for (Map.Entry<String, Index> entry : indexMap.entrySet()) {
                indexes.add(entry.getValue());
            }
        }

        //remove PK indexes
        Set<Index> indexesToRemove = new HashSet<Index>();
        for (Index index : indexes) {
            for (PrimaryKey pk : primaryKeys) {
                if (index.getTable().getName().equalsIgnoreCase(pk.getTable().getName())
                        && index.getColumnNames().equals(pk.getColumnNames())) {
                    indexesToRemove.add(index);
                }
            }
        }
        indexes.removeAll(indexesToRemove);
    }
	
	protected void readSequences(String schema) throws JDBCException {
        updateListeners("Reading sequences for " + database.toString() + " ...");
        
        String convertedSchemaName = database.convertRequestedSchemaToSchema(schema);

        if (database.supportsSequences()) {
            //noinspection unchecked
            List<String> sequenceNamess = (List<String>) database.getJdbcTemplate().queryForList(database.createFindSequencesSQL(schema), String.class, new ArrayList<SqlVisitor>());


            for (String sequenceName : sequenceNamess) {
                Sequence seq = new Sequence();
                seq.setName(sequenceName.trim());
                seq.setName(convertedSchemaName);

                sequences.add(seq);
            }
        }
    }

}
