package liquibase.database.typeconversion.core;

import org.junit.Test;
import static org.junit.Assert.*;
import liquibase.database.structure.type.DataType;
import liquibase.database.structure.type.UnknownType;

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
        assertTypesEqual(new UnknownType("BYTEA", false), new PostgresTypeConverter().getBlobType());
    }

    @Test
    public void getBooleanType() {
        assertTypesEqual(new UnknownType("BOOLEAN", false), new PostgresTypeConverter().getBooleanType());
    }

    @Test
    public void getCurrencyType() {
        assertTypesEqual(new UnknownType("DECIMAL", true), new PostgresTypeConverter().getCurrencyType());
    }

    @Test
    public void getUUIDType() {
        assertTypesEqual(new UnknownType("CHAR(36)", false), new PostgresTypeConverter().getUUIDType());
    }

    @Test
    public void getClobType() {
        assertTypesEqual(new UnknownType("TEXT", true), new PostgresTypeConverter().getClobType());
    }

    @Test
    public void getDateType() {
        assertTypesEqual(new UnknownType("DATE", false), new PostgresTypeConverter().getDateType());
    }

    @Test
    public void getDateTimeType() {
        assertTypesEqual(new UnknownType("TIMESTAMP WITH TIME ZONE", false), new PostgresTypeConverter().getDateTimeType());
    }

       @Test
    public void getColumnType_BigSerial_AutoIncrement() {
        assertEquals("bigserial", new PostgresTypeConverter().getColumnType("bigserial", Boolean.TRUE));
    }

    @Test
    public void getColumnType_BigInt_AutoIncrement() {
        assertEquals("bigserial", new PostgresTypeConverter().getColumnType("bigint", Boolean.TRUE));
    }
}
