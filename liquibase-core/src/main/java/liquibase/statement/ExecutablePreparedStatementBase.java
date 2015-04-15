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
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.resource.ResourceAccessor;
import liquibase.resource.UtfBomAwareReader;
import liquibase.util.JdbcUtils;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;
import liquibase.util.file.FilenameUtils;

public abstract class ExecutablePreparedStatementBase implements ExecutablePreparedStatement {

  private Logger log = LogFactory.getLogger();

	protected Database database;
	private String catalogName;
	private String schemaName;
	private String tableName;
	private List<ColumnConfig> columns;
	private ChangeSet changeSet;

	private Set<Closeable> closeables;
	
	private ResourceAccessor resourceAccessor;

	protected ExecutablePreparedStatementBase(Database database, String catalogName, String schemaName, String tableName, List<ColumnConfig> columns, ChangeSet changeSet, ResourceAccessor resourceAccessor) {
		this.database = database;
		this.changeSet = changeSet;
		this.catalogName = catalogName;
		this.schemaName = schemaName;
		this.tableName = tableName;
		this.columns = columns;
		this.changeSet = changeSet;
		this.closeables = new HashSet<Closeable>();
		this.resourceAccessor = resourceAccessor;
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
	        JdbcUtils.closeStatement(stmt);
	    }
	}

	protected abstract String generateSql(List<ColumnConfig> cols);
	
	private void applyColumnParameter(PreparedStatement stmt, int i, ColumnConfig col) throws SQLException, DatabaseException {
		if(col.getValue() != null) {
        log.debug("value is string = "+col.getValue());
		    stmt.setString(i, col.getValue());
		} else if(col.getValueBoolean() != null) {
        log.debug("value is boolean = "+col.getValueBoolean());
		    stmt.setBoolean(i, col.getValueBoolean());
		} else if(col.getValueNumeric() != null) {
        log.debug("value is numeric = "+col.getValueNumeric());
		    Number number = col.getValueNumeric();
        if(number instanceof ColumnConfig.ValueNumeric) {
            ColumnConfig.ValueNumeric valueNumeric = (ColumnConfig.ValueNumeric) number;
            number = valueNumeric.getDelegate();
        }
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
		    } else {
            // TODO: Consider throwing an exception here
        }
		} else if(col.getValueDate() != null) {
        log.debug("value is date = "+col.getValueDate());
		    stmt.setDate(i, new java.sql.Date(col.getValueDate().getTime()));
		} else if (col.getValueBlobFile() != null) {
        log.debug("value is blob = "+col.getValueBlobFile());
			try {
				LOBContent<InputStream> lob = toBinaryStream(col.getValueBlobFile());
				if (lob.length <= Integer.MAX_VALUE) {
					stmt.setBinaryStream(i, lob.content, (int) lob.length);
				} else {
					stmt.setBinaryStream(i, lob.content, lob.length);
				}
			} catch (IOException e) {
				throw new DatabaseException(e.getMessage(), e); // wrap
			}
		} else if(col.getValueClobFile() != null) {
			try {
        log.debug("value is clob = "+col.getValueClobFile());
				LOBContent<Reader> lob = toCharacterStream(col.getValueClobFile(), col.getEncoding());
				if (lob.length <= Integer.MAX_VALUE) {
					stmt.setCharacterStream(i, lob.content, (int) lob.length);
				} else {
					stmt.setCharacterStream(i, lob.content, lob.length);
				}
			} catch (IOException e) {
				throw new DatabaseException(e.getMessage(), e); // wrap
			}
		} else {
      // NULL values might intentionally be set into a change, we must also add them to the prepared statement
      log.debug("value is explicit null");
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

	@SuppressWarnings("resource")
    private LOBContent<InputStream> toBinaryStream(String valueLobFile) throws DatabaseException, IOException
	{
		InputStream in = getResourceAsStream(valueLobFile);
		
		if (in == null) {
			throw new DatabaseException("BLOB resource not found: " + valueLobFile);
		}
		
		try {
			if (in instanceof FileInputStream) {
				in = createStream(in);
				return new LOBContent<InputStream>(in, ((FileInputStream) in).getChannel().size());
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
				in = getResourceAsStream(valueLobFile);
				in = createStream(in);
			}
			
			return new LOBContent<InputStream>(in, length);
		} finally {
			if (in != null) {
				closeables.add(in);
			}
		}
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
		
		Reader reader = null;
		
		try {
			reader = createReader(in, encoding);
			
			if (reader.markSupported()) {
				reader.mark(IN_MEMORY_THRESHOLD);
			}
			
			long length = StreamUtil.getContentLength(reader);
			
			if (reader.markSupported() && length <= IN_MEMORY_THRESHOLD) {
				reader.reset();
			} else {
				StreamUtil.closeQuietly(reader);
				in = getResourceAsStream(valueLobFile);
				reader = createReader(in, encoding);
			}
			
			return new LOBContent<Reader>(reader, length);
		}
		finally {
			if (reader != null) {
				closeables.add(reader);
			}
			if (in != null) {
				closeables.add(in);
			}
		}
	}

	@SuppressWarnings("resource")
	private Reader createReader(InputStream in, String encoding) throws UnsupportedEncodingException {
		return new BufferedReader(
				StringUtils.trimToNull(encoding) == null
					? new UtfBomAwareReader(in)
					: new UtfBomAwareReader(in, encoding));
	}
	
	private InputStream getResourceAsStream(String valueLobFile) throws IOException {
		String fileName = getFileName(valueLobFile);
        Set<InputStream> streams = this.resourceAccessor.getResourcesAsStream(fileName);
        if (streams == null || streams.size() == 0) {
            return null;
        }
        if (streams.size() > 1) {
            for (InputStream stream : streams) {
                stream.close();
            }

            throw new IOException(streams.size()+ " matched "+valueLobFile);
        }
        return streams.iterator().next();
	}

	private String getFileName(String fileName) {
		//  Most of this method were copy-pasted from XMLChangeLogSAXHandler#handleIncludedChangeLog()
		
		String relativeBaseFileName = changeSet.getChangeLog().getPhysicalFilePath();
		
		// workaround for FilenameUtils.normalize() returning null for relative paths like ../conf/liquibase.xml
		String tempFile = FilenameUtils.concat(FilenameUtils.getFullPath(relativeBaseFileName), fileName);
		if (tempFile != null && new File(tempFile).exists() == true) {
			fileName = tempFile;
		} else {
			fileName = FilenameUtils.getFullPath(relativeBaseFileName) + fileName;
		}
		
		return fileName;
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
