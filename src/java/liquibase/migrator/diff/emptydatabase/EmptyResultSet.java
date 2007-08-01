package liquibase.migrator.diff.emptydatabase;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

class EmptyResultSet implements ResultSet {

    public boolean next() throws SQLException {
        return false;
    }

    public void close() throws SQLException {

    }

    public boolean wasNull() throws SQLException {
        return false;
    }

    public String getString(int columnIndex) throws SQLException {
        return null;
    }

    public boolean getBoolean(int columnIndex) throws SQLException {
        return false;
    }

    public byte getByte(int columnIndex) throws SQLException {
        return 0;
    }

    public short getShort(int columnIndex) throws SQLException {
        return 0;
    }

    public int getInt(int columnIndex) throws SQLException {
        return 0;
    }

    public long getLong(int columnIndex) throws SQLException {
        return 0;
    }

    public float getFloat(int columnIndex) throws SQLException {
        return 0;
    }

    public double getDouble(int columnIndex) throws SQLException {
        return 0;
    }

    @Deprecated
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        return null;
    }

    public byte[] getBytes(int columnIndex) throws SQLException {
        return new byte[0];
    }

    public Date getDate(int columnIndex) throws SQLException {
        return null;
    }

    public Time getTime(int columnIndex) throws SQLException {
        return null;
    }

    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return null;
    }

    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        return null;
    }

    @Deprecated
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        return null;
    }

    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        return null;
    }

    public String getString(String columnName) throws SQLException {
        return null;
    }

    public boolean getBoolean(String columnName) throws SQLException {
        return false;
    }

    public byte getByte(String columnName) throws SQLException {
        return 0;
    }

    public short getShort(String columnName) throws SQLException {
        return 0;
    }

    public int getInt(String columnName) throws SQLException {
        return 0;
    }

    public long getLong(String columnName) throws SQLException {
        return 0;
    }

    public float getFloat(String columnName) throws SQLException {
        return 0;
    }

    public double getDouble(String columnName) throws SQLException {
        return 0;
    }

    @Deprecated
    public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
        return null;
    }

    public byte[] getBytes(String columnName) throws SQLException {
        return new byte[0];
    }

    public Date getDate(String columnName) throws SQLException {
        return null;
    }

    public Time getTime(String columnName) throws SQLException {
        return null;
    }

    public Timestamp getTimestamp(String columnName) throws SQLException {
        return null;
    }

    public InputStream getAsciiStream(String columnName) throws SQLException {
        return null;
    }

    @Deprecated
    public InputStream getUnicodeStream(String columnName) throws SQLException {
        return null;
    }

    public InputStream getBinaryStream(String columnName) throws SQLException {
        return null;
    }

    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    public void clearWarnings() throws SQLException {

    }

    public String getCursorName() throws SQLException {
        return null;
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return null;
    }

    public Object getObject(int columnIndex) throws SQLException {
        return null;
    }

    public Object getObject(String columnName) throws SQLException {
        return null;
    }

    public int findColumn(String columnName) throws SQLException {
        return 0;
    }

    public Reader getCharacterStream(int columnIndex) throws SQLException {
        return null;
    }

    public Reader getCharacterStream(String columnName) throws SQLException {
        return null;
    }

    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return null;
    }

    public BigDecimal getBigDecimal(String columnName) throws SQLException {
        return null;
    }

    public boolean isBeforeFirst() throws SQLException {
        return false;
    }

    public boolean isAfterLast() throws SQLException {
        return false;
    }

    public boolean isFirst() throws SQLException {
        return false;
    }

    public boolean isLast() throws SQLException {
        return false;
    }

    public void beforeFirst() throws SQLException {

    }

    public void afterLast() throws SQLException {

    }

    public boolean first() throws SQLException {
        return false;
    }

    public boolean last() throws SQLException {
        return false;
    }

    public int getRow() throws SQLException {
        return 0;
    }

    public boolean absolute(int row) throws SQLException {
        return false;
    }

    public boolean relative(int rows) throws SQLException {
        return false;
    }

    public boolean previous() throws SQLException {
        return false;
    }

    public void setFetchDirection(int direction) throws SQLException {

    }

    public int getFetchDirection() throws SQLException {
        return 0;
    }

    public void setFetchSize(int rows) throws SQLException {

    }

    public int getFetchSize() throws SQLException {
        return 0;
    }

    public int getType() throws SQLException {
        return 0;
    }

    public int getConcurrency() throws SQLException {
        return 0;
    }

    public boolean rowUpdated() throws SQLException {
        return false;
    }

    public boolean rowInserted() throws SQLException {
        return false;
    }

    public boolean rowDeleted() throws SQLException {
        return false;
    }

    public void updateNull(int columnIndex) throws SQLException {

    }

    public void updateBoolean(int columnIndex, boolean x) throws SQLException {

    }

    public void updateByte(int columnIndex, byte x) throws SQLException {

    }

    public void updateShort(int columnIndex, short x) throws SQLException {

    }

    public void updateInt(int columnIndex, int x) throws SQLException {

    }

    public void updateLong(int columnIndex, long x) throws SQLException {

    }

    public void updateFloat(int columnIndex, float x) throws SQLException {

    }

    public void updateDouble(int columnIndex, double x) throws SQLException {

    }

    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {

    }

    public void updateString(int columnIndex, String x) throws SQLException {

    }

    public void updateBytes(int columnIndex, byte x[]) throws SQLException {

    }

    public void updateDate(int columnIndex, Date x) throws SQLException {

    }

    public void updateTime(int columnIndex, Time x) throws SQLException {

    }

    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {

    }

    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {

    }

    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {

    }

    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {

    }

    public void updateObject(int columnIndex, Object x, int scale) throws SQLException {

    }

    public void updateObject(int columnIndex, Object x) throws SQLException {

    }

    public void updateNull(String columnName) throws SQLException {

    }

    public void updateBoolean(String columnName, boolean x) throws SQLException {

    }

    public void updateByte(String columnName, byte x) throws SQLException {

    }

    public void updateShort(String columnName, short x) throws SQLException {

    }

    public void updateInt(String columnName, int x) throws SQLException {

    }

    public void updateLong(String columnName, long x) throws SQLException {

    }

    public void updateFloat(String columnName, float x) throws SQLException {

    }

    public void updateDouble(String columnName, double x) throws SQLException {

    }

    public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {

    }

    public void updateString(String columnName, String x) throws SQLException {

    }

    public void updateBytes(String columnName, byte x[]) throws SQLException {

    }

    public void updateDate(String columnName, Date x) throws SQLException {

    }

    public void updateTime(String columnName, Time x) throws SQLException {

    }

    public void updateTimestamp(String columnName, Timestamp x) throws SQLException {

    }

    public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {

    }

    public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {

    }

    public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {

    }

    public void updateObject(String columnName, Object x, int scale) throws SQLException {

    }

    public void updateObject(String columnName, Object x) throws SQLException {

    }

    public void insertRow() throws SQLException {

    }

    public void updateRow() throws SQLException {

    }

    public void deleteRow() throws SQLException {

    }

    public void refreshRow() throws SQLException {

    }

    public void cancelRowUpdates() throws SQLException {

    }

    public void moveToInsertRow() throws SQLException {

    }

    public void moveToCurrentRow() throws SQLException {

    }

    public Statement getStatement() throws SQLException {
        return null;
    }

    public Object getObject(int i, Map<String, Class<?>> map) throws SQLException {
        return null;
    }

    public Ref getRef(int i) throws SQLException {
        return null;
    }

    public Blob getBlob(int i) throws SQLException {
        return null;
    }

    public Clob getClob(int i) throws SQLException {
        return null;
    }

    public Array getArray(int i) throws SQLException {
        return null;
    }

    public Object getObject(String colName, Map<String, Class<?>> map) throws SQLException {
        return null;
    }

    public Ref getRef(String colName) throws SQLException {
        return null;
    }

    public Blob getBlob(String colName) throws SQLException {
        return null;
    }

    public Clob getClob(String colName) throws SQLException {
        return null;
    }

    public Array getArray(String colName) throws SQLException {
        return null;
    }

    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    public Date getDate(String columnName, Calendar cal) throws SQLException {
        return null;
    }

    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    public Time getTime(String columnName, Calendar cal) throws SQLException {
        return null;
    }

    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        return null;
    }

    public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
        return null;
    }

    public URL getURL(int columnIndex) throws SQLException {
        return null;
    }

    public URL getURL(String columnName) throws SQLException {
        return null;
    }

    public void updateRef(int columnIndex, Ref x) throws SQLException {

    }

    public void updateRef(String columnName, Ref x) throws SQLException {

    }

    public void updateBlob(int columnIndex, Blob x) throws SQLException {

    }

    public void updateBlob(String columnName, Blob x) throws SQLException {

    }

    public void updateClob(int columnIndex, Clob x) throws SQLException {

    }

    public void updateClob(String columnName, Clob x) throws SQLException {

    }

    public void updateArray(int columnIndex, Array x) throws SQLException {

    }

    public void updateArray(String columnName, Array x) throws SQLException {

    }


    public RowId getRowId(int columnIndex) throws SQLException {
        return null;
    }

    public RowId getRowId(String columnLabel) throws SQLException {
        return null;
    }

    public void updateRowId(int columnIndex, RowId x) throws SQLException {

    }

    public void updateRowId(String columnLabel, RowId x) throws SQLException {

    }

    public int getHoldability() throws SQLException {
        return 0;
    }

    public boolean isClosed() throws SQLException {
        return false;
    }

    public void updateNString(int columnIndex, String nString) throws SQLException {

    }

    public void updateNString(String columnLabel, String nString) throws SQLException {

    }

    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {

    }

    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {

    }

    public NClob getNClob(int columnIndex) throws SQLException {
        return null;
    }

    public NClob getNClob(String columnLabel) throws SQLException {
        return null;
    }

    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        return null;
    }

    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        return null;
    }

    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {

    }

    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {

    }

    public String getNString(int columnIndex) throws SQLException {
        return null;
    }

    public String getNString(String columnLabel) throws SQLException {
        return null;
    }

    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        return null;
    }

    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        return null;
    }

    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

    }

    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

    }

    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {

    }

    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {

    }

    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {

    }

    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {

    }

    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {

    }

    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {

    }

    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {

    }

    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {

    }

    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {

    }

    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {

    }

    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {

    }

    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {

    }

    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {

    }

    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {

    }

    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {

    }

    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {

    }

    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {

    }

    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {

    }

    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {

    }

    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {

    }

    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {

    }

    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {

    }

    public void updateClob(int columnIndex, Reader reader) throws SQLException {

    }

    public void updateClob(String columnLabel, Reader reader) throws SQLException {

    }

    public void updateNClob(int columnIndex, Reader reader) throws SQLException {

    }

    public void updateNClob(String columnLabel, Reader reader) throws SQLException {

    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
