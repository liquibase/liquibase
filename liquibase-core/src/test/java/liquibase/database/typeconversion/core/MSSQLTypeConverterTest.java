package liquibase.database.typeconversion.core;

import org.junit.Test;
import org.junit.Assert;
import liquibase.database.structure.type.DataType;
import liquibase.database.structure.type.UnknownType;

public class MSSQLTypeConverterTest extends DefaultTypeConverterTest{
    
    @Test
    public void getBlobType() {
        assertTypesEqual(new UnknownType("IMAGE", true), new MSSQLTypeConverter().getBlobType());
    }
    
    @Test
    public void getBooleanType() {
        assertTypesEqual(new UnknownType("BIT", false), new MSSQLTypeConverter().getBooleanType());
    }

    
    @Test
    public void getCurrencyType() {
        assertTypesEqual(new UnknownType("MONEY", false), new MSSQLTypeConverter().getCurrencyType());
    }

    
    @Test
    public void getUUIDType() {
        assertTypesEqual(new UnknownType("UNIQUEIDENTIFIER", false), new MSSQLTypeConverter().getUUIDType());
    }

    
    @Test
    public void getClobType() {
        assertTypesEqual(new UnknownType("TEXT", true), new MSSQLTypeConverter().getClobType());
    }

    
    @Test
    public void getDateType() {
        assertTypesEqual(new UnknownType("SMALLDATETIME", false), new MSSQLTypeConverter().getDateType());
    }

    
    @Test
    public void getDateTimeType() {
        assertTypesEqual(new UnknownType("DATETIME", false), new MSSQLTypeConverter().getDateTimeType());
    }

    
}
