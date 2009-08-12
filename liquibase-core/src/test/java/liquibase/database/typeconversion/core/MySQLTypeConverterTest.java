package liquibase.database.typeconversion.core;

import liquibase.database.structure.type.UnknownType;
import org.junit.Assert;
import org.junit.Test;

public class MySQLTypeConverterTest extends DefaultTypeConverterTest {
    
    @Test
    public void getBlobType() {
        assertTypesEqual(new UnknownType("BLOB", true), new MySQLTypeConverter().getBlobType());
    }

    
    @Test
    public void getBooleanType() {
        assertTypesEqual(new UnknownType("TINYINT(1)", false), new MySQLTypeConverter().getBooleanType());
    }

    
    @Test
    public void getCurrencyType() {
        assertTypesEqual(new UnknownType("DECIMAL", true), new MySQLTypeConverter().getCurrencyType());
    }

    
    @Test
    public void getUUIDType() {
        assertTypesEqual(new UnknownType("CHAR(36)", false), new MySQLTypeConverter().getUUIDType());
    }

    
    @Test
    public void getClobType() {
        assertTypesEqual(new UnknownType("TEXT", true), new MySQLTypeConverter().getClobType());
    }

    
    @Test
    public void getDateType() {
        assertTypesEqual(new UnknownType("DATE", false), new MySQLTypeConverter().getDateType());
    }

    
    @Test
    public void getDateTimeType() {
        assertTypesEqual(new UnknownType("DATETIME", false), new MySQLTypeConverter().getDateTimeType());
    }
    
}
