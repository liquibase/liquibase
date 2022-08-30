package liquibase.statement;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import liquibase.change.ColumnConfig;
import liquibase.exception.DatabaseException;

public class ExecutablePreparedStatementBaseTest {

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
        public void setDate(int parameterIndex, Date x) throws SQLException {}

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
        public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
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
