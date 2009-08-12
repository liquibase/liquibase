package liquibase.database.typeconversion.core;

import org.junit.Test;
import org.junit.Assert;
import liquibase.database.typeconversion.DataType;

public class OracleTypeConverterTest extends DefaultTypeConverterTest {
    
    @Test
    public void getBlobType() {
        Assert.assertEquals(new DataType("BLOB", false), new OracleTypeConverter().getBlobType());
    }
    
    @Test
    public void getBooleanType() {
        Assert.assertEquals(new DataType("NUMBER(1)", false), new OracleTypeConverter().getBooleanType());
    }

    @Test
    public void getCurrencyType() {
        Assert.assertEquals(new DataType("NUMBER(15, 2)", false), new OracleTypeConverter().getCurrencyType());
    }

    @Test
    public void getUUIDType() {
        Assert.assertEquals(new DataType("RAW(16)", false), new OracleTypeConverter().getUUIDType());
    }

    @Test
    public void getClobType() {
        Assert.assertEquals(new DataType("CLOB", false), new OracleTypeConverter().getClobType());
    }

    @Test
    public void getDateType() {
        Assert.assertEquals(new DataType("DATE", false), new OracleTypeConverter().getDateType());
    }

    @Test
    public void getDateTimeType() {
        Assert.assertEquals(new DataType("TIMESTAMP", true), new OracleTypeConverter().getDateTimeType());
    }
    
}
