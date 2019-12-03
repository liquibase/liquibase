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
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
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
    private ExecutablePreparedStatementBase.LOBContent<InputStream> blobContentLenghtInteger = new ExecutablePreparedStatementBase.LOBContent(inputStreamMock, Integer.MAX_VALUE - 1);
    private ExecutablePreparedStatementBase.LOBContent<InputStream> blobContentLengthLong = new ExecutablePreparedStatementBase.LOBContent(inputStreamMock, Long.MAX_VALUE);
    private IOException ioException = new IOException(valueBlobFile);
    private String valueClobFile = "valueClobFile";
    private String encoding = StandardCharsets.UTF_8.displayName();
    private Reader reader = new InputStreamReader(new ByteArrayInputStream(new byte[]{}));
    private ExecutablePreparedStatementBase.LOBContent<Reader> clobContentLenghtInteger = new ExecutablePreparedStatementBase.LOBContent(reader, Integer.MAX_VALUE - 1);
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

            verify(executablePreparedStatementBase).generateSql(new ArrayList<>());

            verify(preparedStatementFactoryMock).create(ExecutablePreparedStatementBaseMock.generateSql);

            verify(executablePreparedStatementBase).attachParams(new ArrayList<>(), preparedStatementMock);
            verify(executablePreparedStatementBase, never()).executePreparedStatement(any());

            verify(closeableMock).close();

            fail("Should throw DatabaseException");
        } catch (DatabaseException e) {
            assertEquals(sqlException, e.getCause());
        }
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

        doReturn(valueComputed).when(columnConfig1Mock).getValueComputed();

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
    public void testApplyColumnParameter_valueBlobFileLenghtInteger() throws LiquibaseException, SQLException, IOException {

        doReturn(valueBlobFile).when(columnConfig1Mock).getValueBlobFile();

        doReturn(blobContentLenghtInteger).when(executablePreparedStatementBase).toBinaryStream(any());

        executablePreparedStatementBase.applyColumnParameter(preparedStatementMock, position, columnConfig1Mock);

        verify(preparedStatementMock).setBinaryStream(position, blobContentLenghtInteger.getContent(), (int) blobContentLenghtInteger.getLength());
    }

    @Test
    public void testApplyColumnParameter_valueBlobFileLengthLong() throws LiquibaseException, SQLException, IOException {

        doReturn(valueBlobFile).when(columnConfig1Mock).getValueBlobFile();

        doReturn(blobContentLengthLong).when(executablePreparedStatementBase).toBinaryStream(any());

        executablePreparedStatementBase.applyColumnParameter(preparedStatementMock, position, columnConfig1Mock);

        verify(preparedStatementMock).setBinaryStream(position, blobContentLenghtInteger.getContent(), blobContentLengthLong.getLength());
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

        doReturn(clobContentLenghtInteger).when(executablePreparedStatementBase).toCharacterStream(any(), any());

        executablePreparedStatementBase.applyColumnParameter(preparedStatementMock, position, columnConfig1Mock);

        verify(preparedStatementMock).setCharacterStream(position, clobContentLenghtInteger.getContent(), (int) clobContentLenghtInteger.getLength());
    }

    @Test
    public void testApplyColumnParameter_valueClobFileLengthLong() throws LiquibaseException, SQLException, IOException {

        doReturn(valueClobFile).when(columnConfig1Mock).getValueClobFile();
        doReturn(valueClobFile).when(columnConfig1Mock).getEncoding();

        doReturn(clobContentLengthLong).when(executablePreparedStatementBase).toCharacterStream(any(), any());

        executablePreparedStatementBase.applyColumnParameter(preparedStatementMock, position, columnConfig1Mock);

        verify(preparedStatementMock).setCharacterStream(position, clobContentLenghtInteger.getContent(), clobContentLengthLong.getLength());
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
}
