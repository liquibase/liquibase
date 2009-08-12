package liquibase.database.typeconversion.core;

import org.junit.Test;
import org.junit.Assert;
import liquibase.database.typeconversion.DataType;

public class PostgresTypeConverterTest extends DefaultTypeConverterTest {
    @Test
    public void getFalseBooleanValue() {
        Assert.assertEquals("0", new PostgresTypeConverter().getFalseBooleanValue());
    }

    @Test
    public void getTrueBooleanValue() {
        Assert.assertEquals("1", new PostgresTypeConverter().getTrueBooleanValue());
    }

    @Test
    public void getBlobType() {
        Assert.assertEquals(new DataType("BYTEA", false), new PostgresTypeConverter().getBlobType());
    }

    @Test
    public void getBooleanType() {
        Assert.assertEquals(new DataType("BOOLEAN", false), new PostgresTypeConverter().getBooleanType());
    }

    @Test
    public void getCurrencyType() {
        Assert.assertEquals(new DataType("DECIMAL", true), new PostgresTypeConverter().getCurrencyType());
    }

    @Test
    public void getUUIDType() {
        Assert.assertEquals(new DataType("CHAR(36)", false), new PostgresTypeConverter().getUUIDType());
    }

    @Test
    public void getClobType() {
        Assert.assertEquals(new DataType("TEXT", true), new PostgresTypeConverter().getClobType());
    }

    @Test
    public void getDateType() {
        Assert.assertEquals(new DataType("DATE", false), new PostgresTypeConverter().getDateType());
    }

    @Test
    public void getDateTimeType() {
        Assert.assertEquals(new DataType("TIMESTAMP WITH TIME ZONE", false), new PostgresTypeConverter().getDateTimeType());
    }

       @Test
    public void getColumnType_BigSerial_AutoIncrement() {
        Assert.assertEquals("bigserial", new PostgresTypeConverter().getColumnType("bigserial", Boolean.TRUE));
    }

    @Test
    public void getColumnType_BigInt_AutoIncrement() {
        Assert.assertEquals("bigserial", new PostgresTypeConverter().getColumnType("bigint", Boolean.TRUE));
    }
}
