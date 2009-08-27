package liquibase.database.typeconversion.core;

import org.junit.Test;
import static org.junit.Assert.*;
import liquibase.database.structure.type.CustomType;

public class OracleTypeConverterTest extends DefaultTypeConverterTest {
    
    @Test
    public void getBlobType() {
        assertEquals("BLOB", new OracleTypeConverter().getBlobType().toString());
    }
    
    @Test
    public void getBooleanType() {
        assertEquals("NUMBER(1)", new OracleTypeConverter().getBooleanType().toString());
    }

    @Test
    public void getCurrencyType() {
        assertEquals("NUMBER(15, 2)", new OracleTypeConverter().getCurrencyType().toString());
    }

    @Test
    public void getUUIDType() {
        assertEquals("RAW(16)", new OracleTypeConverter().getUUIDType().toString());
    }

    @Test
    public void getClobType() {
        assertEquals("CLOB", new OracleTypeConverter().getClobType().toString());
    }

    @Test
    public void getDateType() {
        assertEquals("DATE", new OracleTypeConverter().getDateType().toString());
    }

    @Test
    public void getDateTimeType() {
        assertEquals("TIMESTAMP", new OracleTypeConverter().getDateTimeType().toString());
    }
    
}
