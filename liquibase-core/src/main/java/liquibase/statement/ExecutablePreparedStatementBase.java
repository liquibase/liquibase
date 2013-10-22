package liquibase.statement;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import liquibase.change.ColumnConfig;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.PreparedStatementFactory;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.DatabaseException;
import liquibase.util.file.FilenameUtils;
import liquibase.util.StreamUtil;

public abstract class ExecutablePreparedStatementBase implements ExecutablePreparedStatement {

	protected Database database;
	private String catalogName;
	private String schemaName;
	private String tableName;
	private List<ColumnConfig> columns;
	private ChangeSet changeSet;

	protected ExecutablePreparedStatementBase(Database database, String catalogName, String schemaName, String tableName, List<ColumnConfig> columns, ChangeSet changeSet) {
		this.database = database;
		this.catalogName = catalogName;
		this.schemaName = schemaName;
		this.tableName = tableName;
		this.columns = columns;
		this.changeSet = changeSet;
	}

	@Override
    public void execute(PreparedStatementFactory factory) throws DatabaseException {
		
	    // build the sql statement
		List<ColumnConfig> cols = new ArrayList<ColumnConfig>(getColumns().size());
		
	    String sql = generateSql(cols);
	
	    // create prepared statement
	    PreparedStatement stmt = factory.create(sql);
	
	    try {
	        // attach params
	        int i = 1;  // index starts from 1
	        for(ColumnConfig col : cols) {
	            applyColumnParameter(stmt, i, col);
	            i++;
	        }
	        // trigger execution
	        stmt.execute();
	    } catch(SQLException e) {
	        throw new DatabaseException(e);
	    }
	}

	protected abstract String generateSql(List<ColumnConfig> cols);
	
	private void applyColumnParameter(PreparedStatement stmt, int i, ColumnConfig col) throws SQLException, DatabaseException {
		if(col.getValue() != null) {
		    stmt.setString(i, col.getValue());
		} else if(col.getValueBoolean() != null) {
		    stmt.setBoolean(i, col.getValueBoolean());
		} else if(col.getValueNumeric() != null) {
		    Number number = col.getValueNumeric();
		    if(number instanceof Long) {
		        stmt.setLong(i, number.longValue());
		    } else if(number instanceof Integer) {
		        stmt.setInt(i, number.intValue());
		    } else if(number instanceof Double) {
		        stmt.setDouble(i, number.doubleValue());
		    } else if(number instanceof Float) {
		        stmt.setFloat(i, number.floatValue());
		    } else if(number instanceof BigDecimal) {
		        stmt.setBigDecimal(i, (BigDecimal)number);
		    } else if(number instanceof BigInteger) {
		        stmt.setInt(i, number.intValue());
		    }
		} else if(col.getValueDate() != null) {
		    stmt.setDate(i, new java.sql.Date(col.getValueDate().getTime()));
		} else if(col.getValueBlobFile() != null) {
		    try {
                // Add change log base path if file path is relative.
		    	String filePath = getAbsolutePath(col.getValueBlobFile());
		        File file = new File(filePath);
		        stmt.setBinaryStream(i, new BufferedInputStream(new FileInputStream(file)), (int) file.length());
		    } catch (FileNotFoundException e) {
		        throw new DatabaseException(e.getMessage(), e); // wrap
		    }
		} else if(col.getValueClobFile() != null) {
		    try {
                // Add change log base path if file path is relative.
		    	String filePath = getAbsolutePath(col.getValueClobFile());
		        File file = new File(filePath);
		        Reader bufReader = new BufferedReader(new FileReader(file));
		        // PostgreSql does not support PreparedStatement.setCharacterStream() nor
		        // PreparedStatement.setClob().
		        if (database instanceof PostgresDatabase) {
		            String text = StreamUtil.getReaderContents(bufReader);
		            stmt.setString(i, text);
		        } else {
		            stmt.setCharacterStream(i, bufReader);
		        }
		    } catch(FileNotFoundException e) {
		        throw new DatabaseException(e.getMessage(), e); // wrap
		    } catch(IOException e) {
		        throw new DatabaseException(e.getMessage(), e); // wrap
		    }
		} else {
			// NULL values might intentionally be set into a change, we must also add them to the prepared statement  
			stmt.setNull(i, java.sql.Types.NULL);
		}
	}

    /**
     * Gets absolute and normalized path for path.
     * If path is relative, absolute path is calculated relative to change log file.
     *
     * @param path Absolute or relative path.
     * @return Absolute and normalized path.
     */
    public String getAbsolutePath(String path) {
    	String p = path;
        File f = new File(p);
        if (!f.isAbsolute()) {
            String basePath = FilenameUtils.getFullPath(changeSet.getChangeLog().getPhysicalFilePath());
            p = FilenameUtils.normalize(basePath + p);
        }
        return p;
    }


	@Override
    public boolean skipOnUnsupported() {
	    return false;
	}

	public String getCatalogName() {
	    return catalogName;
	}

	public String getSchemaName() {
	    return schemaName;
	}

	public String getTableName() {
	    return tableName;
	}

	public List<ColumnConfig> getColumns() {
	    return columns;
	}

}
