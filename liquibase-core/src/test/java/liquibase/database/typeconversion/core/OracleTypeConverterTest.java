package liquibase.database.typeconversion.core;

import org.junit.Test;
import org.junit.Assert;
import liquibase.database.structure.type.DataType;
import liquibase.database.structure.type.UnknownType;

public class OracleTypeConverterTest extends DefaultTypeConverterTest {
    
    @Test
    public void getBlobType() {
        assertTypesEqual(new UnknownType("BLOB", false), new OracleTypeConverter().getBlobType());
    }
    
    @Test
    public void getBooleanType() {
        assertTypesEqual(new UnknownType("NUMBER(1)", false), new OracleTypeConverter().getBooleanType());
    }

    @Test
    public void getCurrencyType() {
        assertTypesEqual(new UnknownType("NUMBER(15, 2)", false), new OracleTypeConverter().getCurrencyType());
    }

    @Test
    public void getUUIDType() {
        assertTypesEqual(new UnknownType("RAW(16)", false), new OracleTypeConverter().getUUIDType());
    }

    @Test
    public void getClobType() {
        assertTypesEqual(new UnknownType("CLOB", false), new OracleTypeConverter().getClobType());
    }

    @Test
    public void getDateType() {
        assertTypesEqual(new UnknownType("DATE", false), new OracleTypeConverter().getDateType());
    }

    @Test
    public void getDateTimeType() {
        assertTypesEqual(new UnknownType("TIMESTAMP", true), new OracleTypeConverter().getDateTimeType());
    }
    
}
