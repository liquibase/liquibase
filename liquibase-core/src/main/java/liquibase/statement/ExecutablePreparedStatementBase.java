package liquibase.statement;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import liquibase.change.ColumnConfig;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.PreparedStatementFactory;
import liquibase.exception.DatabaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.resource.UtfBomAwareReader;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;
import liquibase.util.file.FilenameUtils;

public abstract class ExecutablePreparedStatementBase implements ExecutablePreparedStatement {

	private ChangeSet changeSet;
	protected Database database;
	private String catalogName;
	private String schemaName;
	private String tableName;
	private List<ColumnConfig> columns;
	
	private Set<Closeable> closeables;

	protected ExecutablePreparedStatementBase(Database database, ChangeSet changeSet, String catalogName, String schemaName, String tableName, List<ColumnConfig> columns) {
		this.database = database;
		this.changeSet = changeSet;
		this.catalogName = catalogName;
		this.schemaName = schemaName;
		this.tableName = tableName;
		this.columns = columns;
		this.closeables = new HashSet<Closeable>();
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
	    } finally {
	        for (Closeable closeable : closeables) {
                StreamUtil.closeQuietly(closeable);
            }
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
		} else if (col.getValueBlobFile() != null) {
			try {
				LOBContent<InputStream> lob = toBinaryStream(col.getValueBlobFile());
				if (lob.length <= Integer.MAX_VALUE) {
					stmt.setBinaryStream(i, lob.content, (int) lob.length);
				} else {
					stmt.setBinaryStream(i, lob.content, lob.length);
				}
				closeables.add(lob.content);
			} catch (IOException e) {
				throw new DatabaseException(e.getMessage(), e); // wrap
			}
		} else if(col.getValueClobFile() != null) {
			try {
				LOBContent<Reader> lob = toCharacterStream(col.getValueClobFile(), col.getEncoding());
				if (lob.length <= Integer.MAX_VALUE) {
					stmt.setCharacterStream(i, lob.content, (int) lob.length);
				} else {
					stmt.setCharacterStream(i, lob.content, lob.length);
				}
				closeables.add(lob.content);
			}
			catch (IOException e) {
				throw new DatabaseException(e.getMessage(), e); // wrap
			}
		} else {
			// NULL values might intentionally be set into a change, we must also add them to the prepared statement  
			stmt.setNull(i, java.sql.Types.NULL);
		}
	}

	private class LOBContent<T> {
		private final T content;
		private final long length;
		
		LOBContent(T content, long length) {
			this.content = content;
			this.length = length;
		}
	}

	private LOBContent<InputStream> toBinaryStream(String valueLobFile) throws DatabaseException, IOException
	{
		InputStream in = getResourceAsStream(valueLobFile);
		
		if (in == null) {
			throw new DatabaseException("BLOB resource not found: " + valueLobFile);
		}
		
		if (in instanceof FileInputStream) {
			return new LOBContent<InputStream>(createStream(in), ((FileInputStream) in).getChannel().size());
		}
		
		in = createStream(in);
		
		final int IN_MEMORY_THRESHOLD = 100000;
		
		if (in.markSupported()) {
			in.mark(IN_MEMORY_THRESHOLD);
		}
		
		long length = StreamUtil.getContentLength(in);
		
		if (in.markSupported() && length <= IN_MEMORY_THRESHOLD) {
			in.reset();
		} else {
			StreamUtil.closeQuietly(in);
			in = createStream(getResourceAsStream(valueLobFile));
		}
		
		return new LOBContent<InputStream>(in, length);
	}

	private InputStream createStream(InputStream in) {
		return (in instanceof BufferedInputStream) ? in : new BufferedInputStream(in);
	}
	
	private LOBContent<Reader> toCharacterStream(String valueLobFile, String encoding) throws IOException, DatabaseException
	{
		InputStream in = getResourceAsStream(valueLobFile);
		
		if (in == null) {
			throw new DatabaseException("CLOB resource not found: " + valueLobFile);
		}
		
		final int IN_MEMORY_THRESHOLD = 100000;
		
		Reader reader = createReader(in, encoding);
		
		if (reader.markSupported()) {
			reader.mark(IN_MEMORY_THRESHOLD);
		}
		
		long length = StreamUtil.getContentLength(reader);
		
		if (reader.markSupported() && length <= IN_MEMORY_THRESHOLD) {
			reader.reset();
		} else {
			StreamUtil.closeQuietly(reader);
			reader = createReader(getResourceAsStream(valueLobFile), encoding);
		}
		
		return new LOBContent<Reader>(reader, length);
	}

	@SuppressWarnings("resource")
	private Reader createReader(InputStream in, String encoding) throws UnsupportedEncodingException {
		return new BufferedReader(
				StringUtils.trimToNull(encoding) == null
					? new UtfBomAwareReader(in)
					: new UtfBomAwareReader(in, encoding));
	}
	
	protected InputStream getResourceAsStream(String valueLobFile) throws IOException {
		//  The same lookup logic that is in LiquibaseServletListener#executeUpdate()
		
		Thread currentThread = Thread.currentThread();
		ClassLoader contextClassLoader = currentThread.getContextClassLoader();
		ResourceAccessor threadClFO = new ClassLoaderResourceAccessor(contextClassLoader);

		ResourceAccessor clFO = new ClassLoaderResourceAccessor();
		ResourceAccessor fsFO = new FileSystemResourceAccessor();
		
		ResourceAccessor accessor = new CompositeResourceAccessor(clFO, fsFO, threadClFO);
		
		String fileName = getFileName(valueLobFile);
		
		InputStream in = accessor.getResourceAsStream(fileName);
		return in;
	}

	protected String getFileName(String fileName) {
		//  Most of this method were copy-pasted from XMLChangeLogSAXHandler#handleIncludedChangeLog()
		
		String relativeBaseFileName = changeSet.getFilePath();
		
		// workaround for FilenameUtils.normalize() returning null for relative paths like ../conf/liquibase.xml
		String tempFile = FilenameUtils.concat(FilenameUtils.getFullPath(relativeBaseFileName), fileName);
		if (tempFile != null && new File(tempFile).exists() == true) {
			fileName = tempFile;
		} else {
			fileName = FilenameUtils.getFullPath(relativeBaseFileName) + fileName;
		}
		
		return fileName;
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