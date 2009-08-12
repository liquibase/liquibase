package liquibase.database.typeconversion.core;

import org.junit.Test;
import org.junit.Assert;
import liquibase.database.structure.type.DataType;
import liquibase.database.structure.type.UnknownType;

public class H2TypeConverterTest extends DefaultTypeConverterTest {
    
    
    @Test
    public void getBlobType() {
        assertTypesEqual(new UnknownType("LONGVARBINARY", true), new H2TypeConverter().getBlobType());
    }

    
    @Test
    public void getBooleanType() {
        assertTypesEqual(new UnknownType("BOOLEAN", false), new H2TypeConverter().getBooleanType());
    }

    
    @Test
    public void getCurrencyType() {
        assertTypesEqual(new UnknownType("DECIMAL", true), new H2TypeConverter().getCurrencyType());
    }

    
    @Test
    public void getUUIDType() {
        assertTypesEqual(new UnknownType("VARCHAR(36)", false), new H2TypeConverter().getUUIDType());
    }

    
    @Test
    public void getClobType() {
        assertTypesEqual(new UnknownType("LONGVARCHAR", true), new H2TypeConverter().getClobType());
    }

    
    @Test
    public void getDateType() {
        assertTypesEqual(new UnknownType("DATE", false), new H2TypeConverter().getDateType());
    }

    
    @Test
    public void getDateTimeType() {
        assertTypesEqual(new UnknownType("TIMESTAMP", false), new H2TypeConverter().getDateTimeType());
    }

    
}
