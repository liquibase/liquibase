package liquibase.statement;

import liquibase.Scope;
import liquibase.change.ColumnConfig;
import liquibase.change.core.LoadDataChange;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.PreparedStatementFactory;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.listener.SqlListener;
import liquibase.logging.Logger;
import liquibase.resource.ResourceAccessor;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.sql.SqlConfiguration;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.util.FilenameUtil;
import liquibase.util.JdbcUtil;
import liquibase.util.StreamUtil;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;

import static java.util.ResourceBundle.getBundle;

public abstract class ExecutablePreparedStatementBase implements ExecutablePreparedStatement {

    protected static ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");

    protected Database database;
    private final String catalogName;
    private final String schemaName;
    private final String tableName;
    private final List<? extends ColumnConfig> columns;
    private final ChangeSet changeSet;

    private final Set<Closeable> closeables;

    private final ResourceAccessor resourceAccessor;

    //Some databases do extra work on creating prepared statements, so constantly creating new prepared statements is expensive
    //When running through a CSV file, the SQL will be the same within the same file so just storing the last seen prepared statement is all we need
    //Ideally the creation of the prepared statements would happen at the spot where we know we should be re-using it and that code can close it.
    // But that will have to wait for a refactoring of this code.
    // So for now we're trading leaving at most one prepared statement unclosed at the end of the liquibase run for better re-using statements to avoid overhead
    private static final ThreadLocal<PreparedStatement> LAST_PREPARED_STATEMENT = new ThreadLocal<>();
    private static final ThreadLocal<String> LAST_PREPARED_STATEMENT_SQL = new ThreadLocal<>();

    //Cache the executeWithFlags method to avoid reflection overhead
    private static final ThreadLocal<Method> EXECUTE_WITH_FLAGS_METHOD = new ThreadLocal<>();

    private final Map<String, Object> snapshotScratchPad = new HashMap<>();

    protected ExecutablePreparedStatementBase(Database database, String catalogName, String schemaName, String
            tableName, List<? extends ColumnConfig> columns, ChangeSet changeSet, ResourceAccessor resourceAccessor) {
        this.database = database;
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columns = columns;
        this.changeSet = changeSet;
        this.closeables = new HashSet<>();
        this.resourceAccessor = resourceAccessor;
    }

    protected static InputStream createStream(InputStream in) {
        return (in instanceof BufferedInputStream) ? in : new BufferedInputStream(in);
    }

    @Override
    public void execute(PreparedStatementFactory factory) throws DatabaseException {
        final Logger log = Scope.getCurrentScope().getLog(getClass());

        // build the sql statement
        List<ColumnConfig> cols = new ArrayList<>(getColumns().size());

        List<SqlVisitor> sqlVisitors = Optional.ofNullable(changeSet)
                .map(ChangeSet::getSqlVisitors)
                .orElseGet(ArrayList::new);
        String sql = applyVisitors(generateSql(cols), sqlVisitors);
        for (SqlListener listener : Scope.getCurrentScope().getListeners(SqlListener.class)) {
            listener.writeSqlWillRun(sql);
        }
        Level sqlLogLevel = SqlConfiguration.SHOW_AT_LOG_LEVEL.getCurrentValue();
        log.log(sqlLogLevel, sql, null);
        log.fine("Number of columns = " + cols.size());

        PreparedStatement stmt = getCachedStatement(sql);
        if (stmt == null) {
            // create prepared statement
            stmt = factory.create(sql);

            LAST_PREPARED_STATEMENT.set(stmt);
            LAST_PREPARED_STATEMENT_SQL.set(sql);
        } else {
            try {
                stmt.clearParameters();
            } catch (SQLException e) {
                log.fine("Error clearing parameters on prepared statement: " + e.getMessage(), e);
            }
        }

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
        }
    }

    protected String applyVisitors(String sql, List<SqlVisitor> sqlVisitors) {
        String finalSql = sql;
        if (sqlVisitors != null) {
            for (SqlVisitor visitor : sqlVisitors) {
                if (visitor != null) {
                    finalSql = visitor.modifySql(finalSql, database);
                }
            }
        }
        return finalSql;
    }

    protected PreparedStatement getCachedStatement(String sql) {
        if (LAST_PREPARED_STATEMENT.get() == null || LAST_PREPARED_STATEMENT_SQL.get() == null) {
            return null;
        }

        boolean statementIsValid = true;
        if (LAST_PREPARED_STATEMENT_SQL.get().equals(sql)) {
            try {
                if (LAST_PREPARED_STATEMENT.get().isClosed()) {
                    statementIsValid = false;
                }
                if (statementIsValid) {
                    final Connection connection = LAST_PREPARED_STATEMENT.get().getConnection();
                    if (connection == null || connection.isClosed()) {
                        statementIsValid = false;
                    }
                }
            } catch (SQLException e) {
                statementIsValid = false;
            }

        } else {
            statementIsValid = false;
        }

        if (!statementIsValid) {
            JdbcUtil.closeStatement(LAST_PREPARED_STATEMENT.get());
            LAST_PREPARED_STATEMENT.remove();
            LAST_PREPARED_STATEMENT_SQL.remove();
            EXECUTE_WITH_FLAGS_METHOD.remove();
        }

        return LAST_PREPARED_STATEMENT.get();
    }

    protected void executePreparedStatement(PreparedStatement stmt) throws SQLException {
        if (database instanceof PostgresDatabase) {
            //postgresql's default prepared statement setup is slow for normal liquibase usage. Calling with QUERY_ONESHOT seems faster, even when we keep re-calling the same prepared statement for many rows in loadData
            try {
                if (EXECUTE_WITH_FLAGS_METHOD.get() == null) {
                    Method executeWithFlags = stmt.getClass().getMethod("executeWithFlags", int.class);
                    executeWithFlags.setAccessible(true);
                    EXECUTE_WITH_FLAGS_METHOD.set(executeWithFlags);
                }

                EXECUTE_WITH_FLAGS_METHOD.get().invoke(stmt, 1); //QueryExecutor.QUERY_ONESHOT
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                stmt.execute();
            }
        } else {
            stmt.execute();
        }
    }

    /**
     * Sets the list of bind variables for the execution of a DML statement
     *
     * @param cols a list of columns with their designated values
     * @param stmt the PreparedStatement to which the values are to be attached
     * @throws SQLException      if JDBC objects to a setting (non-existent bind number, wrong column type etc.)
     * @throws DatabaseException if an I/O error occurs during the read of LOB values
     */
    protected void attachParams(List<? extends ColumnConfig> cols, PreparedStatement stmt)
            throws SQLException, DatabaseException {
        final Logger LOG = Scope.getCurrentScope().getLog(getClass());
        int i = 1;  // index starts from 1
        for (ColumnConfig col : cols) {
            LOG.fine("Applying column parameter = " + i + " for column " + col.getName());
            if (col.getValueObject() instanceof DatabaseFunction) {
                // It's impossible to add a 'db function' to a prepared statement. Adding functions should be handled while building the SQL statement.
                LOG.fine("Skipping column parameter for " + col.getName() + ", cannot add functions to prepared statements.");
            } else {
                LOG.fine("Applying column parameter = " + i + " for column " + col.getName());
                applyColumnParameter(stmt, i, col);
                i++;
            }
        }
    }

    protected abstract String generateSql(List<ColumnConfig> cols);

    /**
     * Sets a single bind variable for a statement to its designated value
     *
     * @param stmt the PreparedStatement whose parameter is to be set
     * @param i    the parameter index (first bind variable is 1)
     * @param col  a ColumnConfig with information about the column, its type, and the desired value
     * @throws SQLException      if JDBC objects to a setting (non-existent bind number, wrong column type etc.)
     * @throws DatabaseException if an I/O error occurs during the read of LOB values
     */
    @SuppressWarnings("squid:S3776")
    protected void applyColumnParameter(PreparedStatement stmt, int i, ColumnConfig col) throws SQLException,
            DatabaseException {

        final Logger LOG = Scope.getCurrentScope().getLog(getClass());
        if (col.getValue() != null) {
            LOG.fine("value is string/UUID/blob = " + col.getValue());
            if (col.getType() != null && col.getType().equalsIgnoreCase(LoadDataChange.LOAD_DATA_TYPE.UUID.name())) {
                stmt.setObject(i, UUID.fromString(col.getValue()));
            } else if (col.getType() != null && col.getType().equalsIgnoreCase(LoadDataChange.LOAD_DATA_TYPE.OTHER.name())) {
                stmt.setObject(i, col.getValue(), Types.OTHER);
            } else if (LoadDataChange.LOAD_DATA_TYPE.BLOB.name().equalsIgnoreCase(col.getType())) {
                stmt.setBlob(i, new ByteArrayInputStream(Base64.getDecoder().decode(col.getValue())));
            } else if (LoadDataChange.LOAD_DATA_TYPE.CLOB.name().equalsIgnoreCase(col.getType())) {
                try {
                    if (database instanceof PostgresDatabase || database instanceof SQLiteDatabase) {
                        // JDBC driver does not have the .createClob() call implemented yet
                        stmt.setString(i, col.getValue());
                    } else {
                        Clob clobValue = stmt.getConnection().createClob();
                        clobValue.setString(1, col.getValue());
                        stmt.setClob(i, clobValue);
                    }
                } catch (SQLFeatureNotSupportedException e) {
                    stmt.setString(i, col.getValue());
                }
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

                if (database instanceof PostgresDatabase) {
                    String snapshotKeyName = String.format("%s-%s-%s-%s", getCatalogName(), getSchemaName(), getTableName(), col.getName());
                    Column snapshot = (Column) this.getScratchData(snapshotKeyName);
                    if (snapshot == null) {
                        snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(
                                new Column(Table.class, getCatalogName(), getSchemaName(), getTableName(), col.getName()), database);
                        this.setScratchData(snapshotKeyName, snapshot);
                    }

                    if (snapshot.getType().getTypeName().equalsIgnoreCase("bytea")) {
                        if (lob.length <= Integer.MAX_VALUE) {
                            stmt.setBinaryStream(i, lob.content, (int) lob.length);
                        } else {
                            stmt.setBinaryStream(i, lob.content, lob.length);
                        }
                        return;
                    }
                }
                if (lob.length <= Integer.MAX_VALUE) {
                    stmt.setBlob(i, lob.content, (int) lob.length);
                } else {
                    stmt.setBlob(i, lob.content, lob.length);
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
            if (col.getType() == null) {
                stmt.setNull(i, java.sql.Types.NULL);
                return;
            }
            if (col.getType().toLowerCase().contains("datetime")) {
                stmt.setNull(i, java.sql.Types.TIMESTAMP);
            } else {
                //
                // Get the array of aliases and use them to find the
                // correct java.sql.Types constant for the call to setNull
                //
                boolean isSet = false;
                LiquibaseDataType dataType = DataTypeFactory.getInstance().fromDescription(col.getType(), database);
                String[] aliases = dataType.getAliases();
                for (String alias : aliases) {
                    if (!alias.contains("java.sql.Types")) {
                        continue;
                    }
                    String name = alias.replaceAll("java.sql.Types.", "");
                    try {
                        JDBCType jdbcType = Enum.valueOf(JDBCType.class, name);
                        stmt.setNull(i, jdbcType.getVendorTypeNumber());
                        isSet = true;
                    } catch (Exception e) {
                        //
                        // fall back to using java.sql.Types.NULL by catching any exceptions
                        //
                    }
                    break;
                }
                if (!isSet) {
                    LOG.info(String.format("Using java.sql.Types.NULL to set null value for type %s", dataType.getName()));
                    stmt.setNull(i, java.sql.Types.NULL);
                }
            }
        }
    }

    @SuppressWarnings("java:S2583")
    protected LOBContent<InputStream> toBinaryStream(String valueLobFile) throws LiquibaseException, IOException {
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

    @SuppressWarnings("java:S2583")
    protected LOBContent<Reader> toCharacterStream(String valueLobFile, String encoding)
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

    @SuppressWarnings("squid:S2095")
    protected InputStream getResourceAsStream(String valueLobFile) throws IOException {
        String fileName = getFileName(valueLobFile);
        return this.resourceAccessor.getExisting(fileName).openInputStream();
    }

    private String getFileName(String fileName) {
        String relativeBaseFileName = changeSet.getChangeLog().getPhysicalFilePath();

        return FilenameUtil.concat(FilenameUtil.getDirectory(relativeBaseFileName), fileName);
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

    public List<? extends ColumnConfig> getColumns() {
        return columns;
    }

    public ChangeSet getChangeSet() {
        return changeSet;
    }

    public ResourceAccessor getResourceAccessor() {
        return resourceAccessor;
    }

    private Object getScratchData(String key) {
        return snapshotScratchPad.get(key);
    }

    private Object setScratchData(String key, Object data) {
        return snapshotScratchPad.put(key, data);
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

    protected static class LOBContent<T> {
        private final T content;
        private final long length;

        public LOBContent(T content, long length) {
            this.content = content;
            this.length = length;
        }

        public T getContent() {
            return content;
        }

        public long getLength() {
            return length;
        }
    }

}
