package liquibase.statement;

import liquibase.Scope;
import liquibase.change.ColumnConfig;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.PreparedStatementFactory;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.listener.SqlListener;
import liquibase.logging.LogType;
import liquibase.logging.Logger;
import liquibase.resource.InputStreamList;
import liquibase.resource.ResourceAccessor;
import liquibase.util.JdbcUtils;
import liquibase.util.StreamUtil;
import liquibase.util.file.FilenameUtils;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

import static java.util.ResourceBundle.getBundle;
import liquibase.change.core.LoadDataChange;

public abstract class ExecutablePreparedStatementBase implements ExecutablePreparedStatement {

    private static final Logger LOG = Scope.getCurrentScope().getLog(ExecutablePreparedStatementBase.class);
    private static ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");

    protected Database database;
    private String catalogName;
    private String schemaName;
    private String tableName;
    private List<ColumnConfig> columns;
    private ChangeSet changeSet;

    private Set<Closeable> closeables;

    private ResourceAccessor resourceAccessor;

    protected ExecutablePreparedStatementBase(Database database, String catalogName, String schemaName, String
            tableName, List<ColumnConfig> columns, ChangeSet changeSet, ResourceAccessor resourceAccessor) {
        this.database = database;
        this.changeSet = changeSet;
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columns = columns;
        this.changeSet = changeSet;
        this.closeables = new HashSet<>();
        this.resourceAccessor = resourceAccessor;
    }

    private static InputStream createStream(InputStream in) {
        return (in instanceof BufferedInputStream) ? in : new BufferedInputStream(in);
    }

    @Override
    public void execute(PreparedStatementFactory factory) throws DatabaseException {

        // build the sql statement
        List<ColumnConfig> cols = new ArrayList<>(getColumns().size());

        String sql = generateSql(cols);
        for (SqlListener listener : Scope.getCurrentScope().getListeners(SqlListener.class)) {
            listener.writeSqlWillRun(sql);
        }
        LOG.fine("Number of columns = " + cols.size());

        // create prepared statement
        PreparedStatement stmt = factory.create(sql);

        try {
            attachParams(cols, stmt);
            // trigger execution
            executePreparedStatement(stmt);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            for (Closeable closeable : closeables) {
                try {
                    closeable.close();
                } catch (IOException ignore) {
                }
            }
            JdbcUtils.closeStatement(stmt);
        }
    }

    protected void executePreparedStatement(PreparedStatement stmt) throws SQLException {
        stmt.execute();
    }

    /**
     * Sets the list of bind variables for the execution of a DML statement
     * @param cols a list of columns with their designated values
     * @param stmt the PreparedStatement to which the values are to be attached
     * @throws SQLException if JDBC objects to a setting (non-existent bind number, wrong column type etc.)
     * @throws DatabaseException if an I/O error occurs during the read of LOB values
     */
    protected void attachParams(List<ColumnConfig> cols, PreparedStatement stmt)
            throws SQLException, DatabaseException {
        int i = 1;  // index starts from 1
        for (ColumnConfig col : cols) {
            LOG.fine("Applying column parameter = " + i + " for column " + col.getName());
            applyColumnParameter(stmt, i, col);
            i++;
        }
    }

    protected abstract String generateSql(List<ColumnConfig> cols);

    /**
     * Sets a single bind variable for a statement to its designated value
     * @param stmt the PreparedStatement whose parameter is to be set
     * @param i the parameter index (first bind variable is 1)
     * @param col a ColumnConfig with information about the column, its type, and the desired value
     * @throws SQLException if JDBC objects to a setting (non-existent bind number, wrong column type etc.)
     * @throws DatabaseException if an I/O error occurs during the read of LOB values
     */
    private void applyColumnParameter(PreparedStatement stmt, int i, ColumnConfig col) throws SQLException,
            DatabaseException {
        if (col.getValue() != null) {
            LOG.fine("value is string = " + col.getValue());
            if (col.getType() != null && col.getType().equalsIgnoreCase(LoadDataChange.LOAD_DATA_TYPE.UUID.name())) {
                stmt.setObject(i, UUID.fromString(col.getValue()));
            } else {
                stmt.setString(i, col.getValue());
            }
        } else if (col.getValueBoolean() != null) {
            LOG.fine("value is boolean = " + col.getValueBoolean());
            stmt.setBoolean(i, col.getValueBoolean());
        } else if (col.getValueNumeric() != null) {
            LOG.fine("value is numeric = " + col.getValueNumeric());
            Number number = col.getValueNumeric();
            if (number instanceof ColumnConfig.ValueNumeric) {
                ColumnConfig.ValueNumeric valueNumeric = (ColumnConfig.ValueNumeric) number;
                number = valueNumeric.getDelegate();
            }
            if (number instanceof Long) {
                stmt.setLong(i, number.longValue());
            } else if (number instanceof Integer) {
                stmt.setInt(i, number.intValue());
            } else if (number instanceof Double) {
                stmt.setDouble(i, number.doubleValue());
            } else if (number instanceof Float) {
                stmt.setFloat(i, number.floatValue());
            } else if (number instanceof BigDecimal) {
                stmt.setBigDecimal(i, (BigDecimal) number);
            } else if (number instanceof BigInteger) {
                stmt.setInt(i, number.intValue());
            } else {
                throw new UnexpectedLiquibaseException(
                        String.format(
                                coreBundle.getString("jdbc.bind.parameter.unknown.numeric.value.type"),
                                col.getName(),
                                col.getValueNumeric().toString(),
                            col.getValueNumeric().getClass().getName()
                        )
                );
            }
        } else if (col.getValueDate() != null) {
            LOG.fine("value is date = " + col.getValueDate());
            if (col.getValueDate() instanceof Timestamp) {
                stmt.setTimestamp(i, (Timestamp) col.getValueDate());
            } else if (col.getValueDate() instanceof Time) {
                stmt.setTime(i, (Time) col.getValueDate());
            } else {
                stmt.setDate(i, new java.sql.Date(col.getValueDate().getTime()));
            }
        } else if (col.getValueBlobFile() != null) {
            LOG.fine("value is blob = " + col.getValueBlobFile());
            try {
                LOBContent<InputStream> lob = toBinaryStream(col.getValueBlobFile());
                if (lob.length <= Integer.MAX_VALUE) {
                    stmt.setBinaryStream(i, lob.content, (int) lob.length);
                } else {
                    stmt.setBinaryStream(i, lob.content, lob.length);
                }
            } catch (IOException | LiquibaseException e) {
                throw new DatabaseException(e.getMessage(), e); // wrap
            }
        } else if (col.getValueClobFile() != null) {
            try {
                LOG.fine("value is clob = " + col.getValueClobFile());
                LOBContent<Reader> lob = toCharacterStream(col.getValueClobFile(), col.getEncoding());
                if (lob.length <= Integer.MAX_VALUE) {
                    stmt.setCharacterStream(i, lob.content, (int) lob.length);
                } else {
                    stmt.setCharacterStream(i, lob.content, lob.length);
                }
            } catch (IOException | LiquibaseException e) {
                throw new DatabaseException(e.getMessage(), e); // wrap
            }
        } else {
            // NULL values might intentionally be set into a change, we must also add them to the prepared statement
            LOG.fine("value is explicit null");
            stmt.setNull(i, java.sql.Types.NULL);
        }
    }

    private LOBContent<InputStream> toBinaryStream(String valueLobFile) throws LiquibaseException, IOException {
        InputStream in = getResourceAsStream(valueLobFile);

        if (in == null) {
            throw new DatabaseException("BLOB resource not found: " + valueLobFile);
        }

        try {
            if (in instanceof FileInputStream) {
                InputStream bufferedInput = createStream(in);
                return new LOBContent<>(bufferedInput, ((FileInputStream) in).getChannel().size());
            }

            in = createStream(in);

            final int IN_MEMORY_THRESHOLD = 100_000;

            if (in.markSupported()) {
                in.mark(IN_MEMORY_THRESHOLD);
            }

            long length = getContentLength(in);

            if (in.markSupported() && (length <= IN_MEMORY_THRESHOLD)) {
                in.reset();
            } else {
                try {
                    in.close();
                } catch (IOException ignored) {

                }
                in = getResourceAsStream(valueLobFile);
                in = createStream(in);
            }

            return new LOBContent<>(in, length);
        } finally {
            if (in != null) {
                closeables.add(in);
            }
        }
    }

    private LOBContent<Reader> toCharacterStream(String valueLobFile, String encoding)
            throws IOException, LiquibaseException {
        InputStream in = getResourceAsStream(valueLobFile);

        if (in == null) {
            throw new DatabaseException("CLOB resource not found: " + valueLobFile);
        }

        final int IN_MEMORY_THRESHOLD = 100_000;

        Reader reader = null;

        try {
            reader = StreamUtil.readStreamWithReader(in, encoding);

            if (reader.markSupported()) {
                reader.mark(IN_MEMORY_THRESHOLD);
            }

            long length = getContentLength(reader);

            if (reader.markSupported() && (length <= IN_MEMORY_THRESHOLD)) {
                reader.reset();
            } else {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
                in = getResourceAsStream(valueLobFile);
                reader = StreamUtil.readStreamWithReader(in, encoding);
            }

            return new LOBContent<>(reader, length);
        } finally {
            if (reader != null) {
                closeables.add(reader);
            }
            if (in != null) {
                closeables.add(in);
            }
        }
    }

    private InputStream getResourceAsStream(String valueLobFile) throws IOException, LiquibaseException {
        String fileName = getFileName(valueLobFile);
        InputStreamList streams = this.resourceAccessor.openStreams(null, fileName);
        if ((streams == null) || streams.isEmpty()) {
            return null;
        }
        if (streams.size() > 1) {
            for (InputStream stream : streams) {
                stream.close();
            }

            throw new IOException(streams.size() + " matched " + valueLobFile);
        }
        return streams.iterator().next();
    }

    private String getFileName(String fileName) {
        //  Most of this method were copy-pasted from XMLChangeLogSAXHandler#handleIncludedChangeLog()

        String relativeBaseFileName = changeSet.getChangeLog().getPhysicalFilePath();

        // workaround for FilenameUtils.normalize() returning null for relative paths like ../conf/liquibase.xml
        String tempFile = FilenameUtils.concat(FilenameUtils.getFullPath(relativeBaseFileName), fileName);
        if (tempFile != null) {
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

    protected long getContentLength(InputStream in) throws IOException {
        long length = 0;
        byte[] buf = new byte[4096];
        int bytesRead = in.read(buf);
        while (bytesRead > 0) {
            length += bytesRead;
            bytesRead = in.read(buf);
        }
        return length;
    }

    protected long getContentLength(Reader reader) throws IOException {
        long length = 0;
        char[] buf = new char[2048];
        int charsRead = reader.read(buf);
        while (charsRead > 0) {
            length += charsRead;
            charsRead = reader.read(buf);
        }
        return length;
    }
    private class LOBContent<T> {
        private final T content;
        private final long length;

        LOBContent(T content, long length) {
            this.content = content;
            this.length = length;
        }
    }

}
