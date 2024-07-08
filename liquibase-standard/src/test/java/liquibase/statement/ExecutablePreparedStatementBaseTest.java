package liquibase.statement;

import liquibase.change.ColumnConfig;
import liquibase.change.core.LoadDataChange;
import liquibase.database.PreparedStatementFactory;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.ReflectionUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.Date;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExecutablePreparedStatementBaseTest {

    @Mock
    private PreparedStatementFactory preparedStatementFactoryMock;

    @Mock
    private PreparedStatement preparedStatementMock;

    @Mock
    private ColumnConfig columnConfig1Mock;

    @Mock
    private ColumnConfig columnConfig2Mock;

    @Mock
    private Closeable closeableMock;

    @Mock
    private InputStream inputStreamMock;

    @Spy
    @InjectMocks
    private ExecutablePreparedStatementBaseMock executablePreparedStatementBase;

    private SQLException sqlException = new SQLException();
    private int position = 1;
    private String valueString = UUID.randomUUID().toString();
    private Boolean valueBoolean = Boolean.TRUE;
    private Long valueLong = 1L;
    private Integer valueInteger = 1;
    private Double valueDouble = 1.0;
    private Float valueFloat = 1.0F;
    private BigDecimal valueBigDecimal = new BigDecimal(1);
    private BigInteger valueBigInteger = new BigInteger("1");
    private Short valueShort = 1;
    private ColumnConfig.ValueNumeric valueNumericDelegate = new ColumnConfig.ValueNumeric("", valueLong);
    private Timestamp valueDateTimestamp = new Timestamp(new Date().getTime());
    private Time valueDateTime = new Time(new Date().getTime());
    private Date valueDateDate = new Date();
    private String valueBlobFile = "valueBlobFile";
    private ExecutablePreparedStatementBase.LOBContent<InputStream> blobContentLengthInteger = new ExecutablePreparedStatementBase.LOBContent(inputStreamMock, Integer.MAX_VALUE - 1);
    private ExecutablePreparedStatementBase.LOBContent<InputStream> blobContentLengthLong = new ExecutablePreparedStatementBase.LOBContent(inputStreamMock, Long.MAX_VALUE);
    private IOException ioException = new IOException(valueBlobFile);
    private String valueClobFile = "valueClobFile";
    private String encoding = StandardCharsets.UTF_8.displayName();
    private Reader reader = new InputStreamReader(new ByteArrayInputStream(new byte[]{}));
    private ExecutablePreparedStatementBase.LOBContent<Reader> clobContentLengthInteger = new ExecutablePreparedStatementBase.LOBContent(reader, Integer.MAX_VALUE - 1);
    private ExecutablePreparedStatementBase.LOBContent<Reader> clobContentLengthLong = new ExecutablePreparedStatementBase.LOBContent(reader, Long.MAX_VALUE);
    private DatabaseFunction valueComputed = new DatabaseFunction("select * from some_table");
    private Object valueComputedObject = new Object();

    @Before
    public void setUp() {

        // mockito returns false for Boolean return type by default
        doReturn(null).when(columnConfig1Mock).getValueBoolean();

        Field columnsField = ReflectionUtils.findField(ExecutablePreparedStatementBase.class, "columns");
        ReflectionUtils.makeAccessible(columnsField);
        ReflectionUtils.setField(columnsField, executablePreparedStatementBase, Collections.singletonList(columnConfig1Mock));

        Field closeablesField = ReflectionUtils.findField(ExecutablePreparedStatementBase.class, "closeables");
        ReflectionUtils.makeAccessible(closeablesField);
        ReflectionUtils.setField(closeablesField, executablePreparedStatementBase, Collections.singleton(closeableMock));
    }

    @Test
    public void testCreateStream() throws IOException {
        BufferedInputStream bufferedInputStream = (BufferedInputStream) ExecutablePreparedStatementBase.createStream(new BufferedInputStream(inputStreamMock));
        bufferedInputStream.close();

        verify(inputStreamMock).close();
    }

    @Test
    public void testCreateStream_NotBufferedInputStream() throws IOException {
        BufferedInputStream bufferedInputStream = (BufferedInputStream) ExecutablePreparedStatementBase.createStream(inputStreamMock);
        bufferedInputStream.close();

        verify(inputStreamMock).close();
    }

    @Test
    public void testExecute() throws DatabaseException, SQLException, IOException {

        doReturn(preparedStatementMock).when(preparedStatementFactoryMock).create(any());

        doNothing().when(executablePreparedStatementBase).attachParams(any(), any());
        doNothing().when(executablePreparedStatementBase).executePreparedStatement(any());

        doNothing().when(closeableMock).close();

        executablePreparedStatementBase.execute(preparedStatementFactoryMock);

        verify(executablePreparedStatementBase).generateSql(new ArrayList<>());

        verify(preparedStatementFactoryMock).create(ExecutablePreparedStatementBaseMock.generateSql);

        verify(executablePreparedStatementBase).attachParams(new ArrayList<>(), preparedStatementMock);
        verify(executablePreparedStatementBase).executePreparedStatement(preparedStatementMock);

        verify(closeableMock).close();
    }

    @Test
    public void testExecute_attachParamsThrowsSQLException() throws DatabaseException, SQLException, IOException {

        doReturn(preparedStatementMock).when(preparedStatementFactoryMock).create(any());

        doThrow(sqlException).when(executablePreparedStatementBase).attachParams(any(), any());

        try {
            executablePreparedStatementBase.execute(preparedStatementFactoryMock);

            fail("Should throw DatabaseException");
        } catch (DatabaseException e) {
            assertEquals(sqlException, e.getCause());
        }

        verify(executablePreparedStatementBase).generateSql(new ArrayList<>());

        verify(preparedStatementFactoryMock).create(ExecutablePreparedStatementBaseMock.generateSql);

        verify(executablePreparedStatementBase).attachParams(new ArrayList<>(), preparedStatementMock);
        verify(executablePreparedStatementBase, never()).executePreparedStatement(any());

        verify(closeableMock).close();
    }

    @Test
    public void testExecute_closeableThrowsIOException() throws DatabaseException, SQLException, IOException {

        doReturn(preparedStatementMock).when(preparedStatementFactoryMock).create(any());

        doNothing().when(executablePreparedStatementBase).attachParams(any(), any());
        doNothing().when(executablePreparedStatementBase).executePreparedStatement(any());

        doThrow(new IOException()).when(closeableMock).close();

        executablePreparedStatementBase.execute(preparedStatementFactoryMock);

        verify(executablePreparedStatementBase).generateSql(new ArrayList<>());

        verify(preparedStatementFactoryMock).create(ExecutablePreparedStatementBaseMock.generateSql);

        verify(executablePreparedStatementBase).attachParams(new ArrayList<>(), preparedStatementMock);
        verify(executablePreparedStatementBase).executePreparedStatement(preparedStatementMock);

        verify(closeableMock).close();
    }

    @Test
    public void testExecutePreparedStatement() throws SQLException {

        doReturn(true).when(preparedStatementMock).execute();

        executablePreparedStatementBase.executePreparedStatement(preparedStatementMock);

        verify(preparedStatementMock).execute();
    }

    @Test
    public void testAttachParams() throws DatabaseException, SQLException {

        doNothing().when(executablePreparedStatementBase).applyColumnParameter(any(), anyInt(), any());

        executablePreparedStatementBase.attachParams(Arrays.asList(columnConfig1Mock, columnConfig2Mock), preparedStatementMock);

        verify(executablePreparedStatementBase).applyColumnParameter(preparedStatementMock, 1, columnConfig1Mock);
        verify(executablePreparedStatementBase).applyColumnParameter(preparedStatementMock, 2, columnConfig2Mock);
    }

    @Test
    public void testAttachParams_ValueComputed() throws DatabaseException, SQLException {

        doReturn(valueComputed).when(columnConfig1Mock).getValueObject();

        doNothing().when(executablePreparedStatementBase).applyColumnParameter(any(), anyInt(), any());

        executablePreparedStatementBase.attachParams(Arrays.asList(columnConfig1Mock, columnConfig2Mock), preparedStatementMock);

        verify(executablePreparedStatementBase).applyColumnParameter(preparedStatementMock, 1, columnConfig2Mock);
        verify(executablePreparedStatementBase, never()).applyColumnParameter(eq(preparedStatementMock), eq(2), any());
    }

    @Test
    public void testApplyColumnParameter_valueString() throws DatabaseException, SQLException {

        doReturn(valueString).when(columnConfig1Mock).getValue();

        executablePreparedStatementBase.applyColumnParameter(preparedStatementMock, position, columnConfig1Mock);

        verify(preparedStatementMock).setString(position, valueString);
    }

    @Test
    public void testApplyColumnParameter_valueStringTypeUnknown() throws DatabaseException, SQLException {

        doReturn(valueString).when(columnConfig1Mock).getValue();
        doReturn(LoadDataChange.LOAD_DATA_TYPE.SKIP.name()).when(columnConfig1Mock).getType();

        executablePreparedStatementBase.applyColumnParameter(preparedStatementMock, position, columnConfig1Mock);

        verify(preparedStatementMock).setString(position, valueString);
    }

    @Test
    public void testApplyColumnParameter_valueStringTypeUuid() throws DatabaseException, SQLException {

        doReturn(valueString).when(columnConfig1Mock).getValue();
        doReturn(LoadDataChange.LOAD_DATA_TYPE.UUID.name()).when(columnConfig1Mock).getType();

        executablePreparedStatementBase.applyColumnParameter(preparedStatementMock, position, columnConfig1Mock);

        verify(preparedStatementMock).setObject(position, UUID.fromString(valueString));
    }

    @Test
    public void testApplyColumnParameter_valueBoolean() throws DatabaseException, SQLException {

        doReturn(valueBoolean).when(columnConfig1Mock).getValueBoolean();

        executablePreparedStatementBase.applyColumnParameter(preparedStatementMock, position, columnConfig1Mock);

        verify(preparedStatementMock).setBoolean(position, valueBoolean);
    }

    @Test
    public void testApplyColumnParameter_valueNumericDelegate() throws DatabaseException, SQLException {

        doReturn(valueNumericDelegate).when(columnConfig1Mock).getValueNumeric();

        executablePreparedStatementBase.applyColumnParameter(preparedStatementMock, position, columnConfig1Mock);

        verify(preparedStatementMock).setLong(position, valueLong);
    }

    @Test
    public void testApplyColumnParameter_valueNumericLong() throws DatabaseException, SQLException {

        doReturn(valueLong).when(columnConfig1Mock).getValueNumeric();

        executablePreparedStatementBase.applyColumnParameter(preparedStatementMock, position, columnConfig1Mock);

        verify(preparedStatementMock).setLong(position, valueLong);
    }

    @Test
    public void testApplyColumnParameter_valueNumericInteger() throws DatabaseException, SQLException {

        doReturn(valueInteger).when(columnConfig1Mock).getValueNumeric();

        executablePreparedStatementBase.applyColumnParameter(preparedStatementMock, position, columnConfig1Mock);

        verify(preparedStatementMock).setInt(position, valueInteger);
    }

    @Test
    public void testApplyColumnParameter_valueNumericDouble() throws DatabaseException, SQLException {

        doReturn(valueDouble).when(columnConfig1Mock).getValueNumeric();

        executablePreparedStatementBase.applyColumnParameter(preparedStatementMock, position, columnConfig1Mock);

        verify(preparedStatementMock).setDouble(position, valueDouble);
    }

    @Test
    public void testApplyColumnParameter_valueNumericFloat() throws DatabaseException, SQLException {

        doReturn(valueFloat).when(columnConfig1Mock).getValueNumeric();

        executablePreparedStatementBase.applyColumnParameter(preparedStatementMock, position, columnConfig1Mock);

        verify(preparedStatementMock).setFloat(position, valueFloat);
    }

    @Test
    public void testApplyColumnParameter_valueNumericBigDecimal() throws DatabaseException, SQLException {

        doReturn(valueBigDecimal).when(columnConfig1Mock).getValueNumeric();

        executablePreparedStatementBase.applyColumnParameter(preparedStatementMock, position, columnConfig1Mock);

        verify(preparedStatementMock).setBigDecimal(position, valueBigDecimal);
    }

    @Test
    public void testApplyColumnParameter_valueNumericBigInteger() throws DatabaseException, SQLException {

        doReturn(valueBigInteger).when(columnConfig1Mock).getValueNumeric();

        executablePreparedStatementBase.applyColumnParameter(preparedStatementMock, position, columnConfig1Mock);

        verify(preparedStatementMock).setInt(position, valueBigInteger.intValue());
    }

    @Test
    public void testApplyColumnParameter_valueNumericUnknown() throws DatabaseException, SQLException {

        doReturn(valueShort).when(columnConfig1Mock).getValueNumeric();

        try {
            executablePreparedStatementBase.applyColumnParameter(preparedStatementMock, position, columnConfig1Mock);

            fail("Should throw UnexpectedLiquibaseException");
        } catch (UnexpectedLiquibaseException e) {
            assertEquals(String.format(
                    ExecutablePreparedStatementBase.coreBundle.getString("jdbc.bind.parameter.unknown.numeric.value.type"),
                    columnConfig1Mock.getName(),
                    columnConfig1Mock.getValueNumeric().toString(),
                    columnConfig1Mock.getValueNumeric().getClass().getName()),
                    e.getMessage());
        }
    }

    @Test
    public void testApplyColumnParameter_valueDateTimestamp() throws DatabaseException, SQLException {

        doReturn(valueDateTimestamp).when(columnConfig1Mock).getValueDate();

        executablePreparedStatementBase.applyColumnParameter(preparedStatementMock, position, columnConfig1Mock);

        verify(preparedStatementMock).setTimestamp(position, valueDateTimestamp);
    }

    @Test
    public void testApplyColumnParameter_valueDateTime() throws DatabaseException, SQLException {

        doReturn(valueDateTime).when(columnConfig1Mock).getValueDate();

        executablePreparedStatementBase.applyColumnParameter(preparedStatementMock, position, columnConfig1Mock);

        verify(preparedStatementMock).setTime(position, valueDateTime);
    }

    @Test
    public void testApplyColumnParameter_valueDateDate() throws DatabaseException, SQLException {

        doReturn(valueDateDate).when(columnConfig1Mock).getValueDate();

        executablePreparedStatementBase.applyColumnParameter(preparedStatementMock, position, columnConfig1Mock);

        verify(preparedStatementMock).setDate(position, new java.sql.Date(valueDateDate.getTime()));
    }

    @Test
    public void testApplyColumnParameter_valueBlobFileLengthInteger() throws LiquibaseException, SQLException, IOException {

        doReturn(valueBlobFile).when(columnConfig1Mock).getValueBlobFile();

        doReturn(blobContentLengthInteger).when(executablePreparedStatementBase).toBinaryStream(any());

        executablePreparedStatementBase.applyColumnParameter(preparedStatementMock, position, columnConfig1Mock);

        verify(preparedStatementMock).setBlob(position, blobContentLengthInteger.getContent(), (int) blobContentLengthInteger.getLength());
    }

    @Test
    public void testApplyColumnParameter_valueBlobFileLengthLong() throws LiquibaseException, SQLException, IOException {

        doReturn(valueBlobFile).when(columnConfig1Mock).getValueBlobFile();

        doReturn(blobContentLengthLong).when(executablePreparedStatementBase).toBinaryStream(any());

        executablePreparedStatementBase.applyColumnParameter(preparedStatementMock, position, columnConfig1Mock);

        verify(preparedStatementMock).setBlob(position, blobContentLengthInteger.getContent(), blobContentLengthLong.getLength());
    }

    @Test
    public void testApplyColumnParameter_valueBlobBinaryStreamThrowsIOException() throws LiquibaseException, SQLException, IOException {

        doReturn(valueBlobFile).when(columnConfig1Mock).getValueBlobFile();

        doThrow(ioException).when(executablePreparedStatementBase).toBinaryStream(any());

        try {
            executablePreparedStatementBase.applyColumnParameter(preparedStatementMock, position, columnConfig1Mock);

            fail("Should throw DatabaseException");
        } catch (DatabaseException e) {
            assertEquals(valueBlobFile, e.getMessage());
            assertEquals(ioException, e.getCause());
        }
    }

    @Test
    public void testApplyColumnParameter_valueClobFileLenghtInteger() throws LiquibaseException, SQLException, IOException {

        doReturn(valueClobFile).when(columnConfig1Mock).getValueClobFile();
        doReturn(valueClobFile).when(columnConfig1Mock).getEncoding();

        doReturn(clobContentLengthInteger).when(executablePreparedStatementBase).toCharacterStream(any(), any());

        executablePreparedStatementBase.applyColumnParameter(preparedStatementMock, position, columnConfig1Mock);

        verify(preparedStatementMock).setCharacterStream(position, clobContentLengthInteger.getContent(), (int) clobContentLengthInteger.getLength());
    }

    @Test
    public void testApplyColumnParameter_valueClobFileLengthLong() throws LiquibaseException, SQLException, IOException {

        doReturn(valueClobFile).when(columnConfig1Mock).getValueClobFile();
        doReturn(valueClobFile).when(columnConfig1Mock).getEncoding();

        doReturn(clobContentLengthLong).when(executablePreparedStatementBase).toCharacterStream(any(), any());

        executablePreparedStatementBase.applyColumnParameter(preparedStatementMock, position, columnConfig1Mock);

        verify(preparedStatementMock).setCharacterStream(position, clobContentLengthInteger.getContent(), clobContentLengthLong.getLength());
    }

    @Test
    public void testApplyColumnParameter_valueClobBinaryStreamThrowsIOException() throws LiquibaseException, SQLException, IOException {

        doReturn(valueClobFile).when(columnConfig1Mock).getValueClobFile();
        doReturn(valueClobFile).when(columnConfig1Mock).getEncoding();

        doThrow(ioException).when(executablePreparedStatementBase).toCharacterStream(any(), any());

        try {
            executablePreparedStatementBase.applyColumnParameter(preparedStatementMock, position, columnConfig1Mock);

            fail("Should throw DatabaseException");
        } catch (DatabaseException e) {
            assertEquals(valueBlobFile, e.getMessage());
            assertEquals(ioException, e.getCause());
        }
    }

    @Test
    public void testApplyColumnParameter_valueNull() throws LiquibaseException, SQLException, IOException {

        executablePreparedStatementBase.applyColumnParameter(preparedStatementMock, position, columnConfig1Mock);

        verify(preparedStatementMock).setNull(position, java.sql.Types.NULL);
    }

    @Test
    public void dateParameterTypeIsDeclared() throws DatabaseException, SQLException {
        ExecutablePreparedStatementBase preparedStatement = new ExecutablePreparedStatementBase(null, null, null, null, null, null, null) {
            @Override
            public boolean continueOnError() {
                return false;
            }

            @Override
            protected String generateSql(List<ColumnConfig> cols) {
                return null;
            }
        };

        DummyPreparedStatement stmt;

        List<ColumnConfig> columns = new ArrayList<>();
        ColumnConfig dateColumn = new ColumnConfig();
        dateColumn.setName("date_column");
        dateColumn.setType("DATE");
        columns.add(dateColumn);

        stmt = new DummyPreparedStatement();
        preparedStatement.attachParams(columns, stmt);
        assertNotEquals(java.sql.Types.NULL, stmt.getParamTypes().get(1).intValue());
        assertEquals(java.sql.Types.DATE, stmt.getParamTypes().get(1).intValue());

        stmt = new DummyPreparedStatement();
        dateColumn.setType("DATETIME");
        preparedStatement.attachParams(columns, stmt);
        assertNotEquals(java.sql.Types.NULL, stmt.getParamTypes().get(1).intValue());
        assertEquals(java.sql.Types.TIMESTAMP, stmt.getParamTypes().get(1).intValue());

        stmt = new DummyPreparedStatement();
        dateColumn.setType("TIMESTAMP");
        preparedStatement.attachParams(columns, stmt);
        assertNotEquals(java.sql.Types.NULL, stmt.getParamTypes().get(1).intValue());
        assertEquals(java.sql.Types.TIMESTAMP, stmt.getParamTypes().get(1).intValue());

        stmt = new DummyPreparedStatement();
        dateColumn.setType("STRING");
        preparedStatement.attachParams(columns, stmt);
        assertEquals(java.sql.Types.NULL, stmt.getParamTypes().get(1).intValue());
    }

    private static class DummyPreparedStatement implements PreparedStatement {
        private Map<Integer, Integer> paramTypes = new HashMap<>();

        public Map<Integer, Integer> getParamTypes() {
            return paramTypes;
        }

        @Override
        public ResultSet executeQuery(String sql) throws SQLException {
            return null;
        }

        @Override
        public int executeUpdate(String sql) throws SQLException {
            return 0;
        }

        @Override
        public void close() throws SQLException {}

        @Override
        public int getMaxFieldSize() throws SQLException {
            return 0;
        }

        @Override
        public void setMaxFieldSize(int max) throws SQLException {}

        @Override
        public int getMaxRows() throws SQLException {
            return 0;
        }

        @Override
        public void setMaxRows(int max) throws SQLException {}

        @Override
        public void setEscapeProcessing(boolean enable) throws SQLException {}

        @Override
        public int getQueryTimeout() throws SQLException {
            return 0;
        }

        @Override
        public void setQueryTimeout(int seconds) throws SQLException {}

        @Override
        public void cancel() throws SQLException {}

        @Override
        public SQLWarning getWarnings() throws SQLException {
            return null;
        }

        @Override
        public void clearWarnings() throws SQLException {}

        @Override
        public void setCursorName(String name) throws SQLException {}

        @Override
        public boolean execute(String sql) throws SQLException {
            return false;
        }

        @Override
        public ResultSet getResultSet() throws SQLException {
            return null;
        }

        @Override
        public int getUpdateCount() throws SQLException {
            return 0;
        }

        @Override
        public boolean getMoreResults() throws SQLException {
            return false;
        }

        @Override
        public void setFetchDirection(int direction) throws SQLException {}

        @Override
        public int getFetchDirection() throws SQLException {
            return 0;
        }

        @Override
        public void setFetchSize(int rows) throws SQLException {}

        @Override
        public int getFetchSize() throws SQLException {
            return 0;
        }

        @Override
        public int getResultSetConcurrency() throws SQLException {
            return 0;
        }

        @Override
        public int getResultSetType() throws SQLException {
            return 0;
        }

        @Override
        public void addBatch(String sql) throws SQLException {}

        @Override
        public void clearBatch() throws SQLException {}

        @Override
        public int[] executeBatch() throws SQLException {
            return null;
        }

        @Override
        public Connection getConnection() throws SQLException {
            return null;
        }

        @Override
        public boolean getMoreResults(int current) throws SQLException {
            return false;
        }

        @Override
        public ResultSet getGeneratedKeys() throws SQLException {
            return null;
        }

        @Override
        public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
            return 0;
        }

        @Override
        public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
            return 0;
        }

        @Override
        public int executeUpdate(String sql, String[] columnNames) throws SQLException {
            return 0;
        }

        @Override
        public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
            return false;
        }

        @Override
        public boolean execute(String sql, int[] columnIndexes) throws SQLException {
            return false;
        }

        @Override
        public boolean execute(String sql, String[] columnNames) throws SQLException {
            return false;
        }

        @Override
        public int getResultSetHoldability() throws SQLException {
            return 0;
        }

        @Override
        public boolean isClosed() throws SQLException {
            return false;
        }

        @Override
        public void setPoolable(boolean poolable) throws SQLException {}

        @Override
        public boolean isPoolable() throws SQLException {
            return false;
        }

        @Override
        public void closeOnCompletion() throws SQLException {}

        @Override
        public boolean isCloseOnCompletion() throws SQLException {
            return false;
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return null;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }

        @Override
        public ResultSet executeQuery() throws SQLException {
            return null;
        }

        @Override
        public int executeUpdate() throws SQLException {
            return 0;
        }

        @Override
        public void setNull(int parameterIndex, int sqlType) throws SQLException {
            paramTypes.put(parameterIndex, sqlType);
        }

        @Override
        public void setBoolean(int parameterIndex, boolean x) throws SQLException {}

        @Override
        public void setByte(int parameterIndex, byte x) throws SQLException {}

        @Override
        public void setShort(int parameterIndex, short x) throws SQLException {}

        @Override
        public void setInt(int parameterIndex, int x) throws SQLException {}

        @Override
        public void setLong(int parameterIndex, long x) throws SQLException {}

        @Override
        public void setFloat(int parameterIndex, float x) throws SQLException {}

        @Override
        public void setDouble(int parameterIndex, double x) throws SQLException {}

        @Override
        public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {}

        @Override
        public void setString(int parameterIndex, String x) throws SQLException {}

        @Override
        public void setBytes(int parameterIndex, byte[] x) throws SQLException {}

        @Override
        public void setDate(int parameterIndex, java.sql.Date x) throws SQLException {}

        @Override
        public void setTime(int parameterIndex, Time x) throws SQLException {}

        @Override
        public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {}

        @Override
        public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {}

        @Override
        public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {}

        @Override
        public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {}

        @Override
        public void clearParameters() throws SQLException {}

        @Override
        public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {}

        @Override
        public void setObject(int parameterIndex, Object x) throws SQLException {}

        @Override
        public boolean execute() throws SQLException {
            return false;
        }

        @Override
        public void addBatch() throws SQLException {}

        @Override
        public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {}

        @Override
        public void setRef(int parameterIndex, Ref x) throws SQLException {}

        @Override
        public void setBlob(int parameterIndex, Blob x) throws SQLException {}

        @Override
        public void setClob(int parameterIndex, Clob x) throws SQLException {}

        @Override
        public void setArray(int parameterIndex, Array x) throws SQLException {}

        @Override
        public ResultSetMetaData getMetaData() throws SQLException {
            return null;
        }

        @Override
        public void setDate(int parameterIndex, java.sql.Date x, Calendar cal) throws SQLException {
        }

        @Override
        public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        }

        @Override
        public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        }

        @Override
        public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
            paramTypes.put(parameterIndex, sqlType);
        }

        @Override
        public void setURL(int parameterIndex, URL x) throws SQLException {
        }

        @Override
        public ParameterMetaData getParameterMetaData() throws SQLException {
            return null;
        }

        @Override
        public void setRowId(int parameterIndex, RowId x) throws SQLException {
        }

        @Override
        public void setNString(int parameterIndex, String value) throws SQLException {
        }

        @Override
        public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        }

        @Override
        public void setNClob(int parameterIndex, NClob value) throws SQLException {
        }

        @Override
        public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        }

        @Override
        public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        }

        @Override
        public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        }

        @Override
        public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        }

        @Override
        public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        }

        @Override
        public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        }

        @Override
        public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        }

        @Override
        public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        }

        @Override
        public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        }

        @Override
        public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        }

        @Override
        public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        }

        @Override
        public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        }

        @Override
        public void setClob(int parameterIndex, Reader reader) throws SQLException {}

        @Override
        public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        }

        @Override
        public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        }
    }

}
