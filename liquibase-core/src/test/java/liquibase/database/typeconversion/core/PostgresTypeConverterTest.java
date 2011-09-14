package liquibase.database.typeconversion.core;

import org.junit.Test;
import static org.junit.Assert.*;
import liquibase.database.structure.type.CustomType;

public class PostgresTypeConverterTest extends DefaultTypeConverterTest {
    @Test
    public void getFalseBooleanValue() {
        assertEquals("FALSE", new PostgresTypeConverter().getBooleanType().getFalseBooleanValue());
    }

    @Test
    public void getTrueBooleanValue() {
        assertEquals("TRUE", new PostgresTypeConverter().getBooleanType().getTrueBooleanValue());
    }

    @Test
    public void getBlobType() {
        assertEquals("BYTEA", new PostgresTypeConverter().getBlobType().toString());
    }

    @Test
    public void getBooleanType() {
        assertEquals("BOOLEAN", new PostgresTypeConverter().getBooleanType().toString());
    }

    @Test
    public void getCurrencyType() {
        assertEquals("DECIMAL", new PostgresTypeConverter().getCurrencyType().toString());
    }

    @Test
    public void getUUIDType() {
        assertEquals("CHAR(36)", new PostgresTypeConverter().getUUIDType().toString());
    }

    @Test
    public void getClobType() {
        assertEquals("TEXT", new PostgresTypeConverter().getClobType().toString());
    }

    @Test
    public void getDateType() {
        assertEquals("DATE", new PostgresTypeConverter().getDateType().toString());
    }

    @Test
    public void getDateTimeType() {
        assertEquals("TIMESTAMP WITH TIME ZONE", new PostgresTypeConverter().getDateTimeType().toString());
    }

    @Test
    public void getColumnType_BigSerial_AutoIncrement() {
        assertEquals("bigserial", new PostgresTypeConverter().getDataType("bigserial", true).toString());
    }

    @Test
    public void getColumnType_BigInt_AutoIncrement() {
        assertEquals("bigserial", new PostgresTypeConverter().getDataType("bigint", true).toString());
    }

    @Test
    public void getColumnType_TinyText() {
        assertEquals("TEXT", new PostgresTypeConverter().getDataType("TINYTEXT", false).toString());
    }

    @Test
    public void getColumnType_Text() {
        assertEquals("TEXT", new PostgresTypeConverter().getDataType("TEXT", false).toString());
    }

    @Test
    public void getColumnType_MediumText() {
        assertEquals("TEXT", new PostgresTypeConverter().getDataType("MEDIUMTEXT", false).toString());
    }

    @Test
    public void getColumnType_LongText() {
        assertEquals("TEXT", new PostgresTypeConverter().getDataType("LONGTEXT", false).toString());
    }
    @Test
    public void getColumnType_TinyBlob() {
        assertEquals("BYTEA", new PostgresTypeConverter().getDataType("TINYBLOB", false).toString());
    }

    @Test
    public void getColumnType_Blob() {
        assertEquals("BYTEA", new PostgresTypeConverter().getDataType("BLOB", false).toString());
    }

    @Test
    public void getColumnType_MediumBlob() {
        assertEquals("BYTEA", new PostgresTypeConverter().getDataType("MEDIUMBLOB", false).toString());
    }

    @Test
    public void getColumnType_LongBlob() {
        assertEquals("BYTEA", new PostgresTypeConverter().getDataType("LONGBLOB", false).toString());
    }
}
