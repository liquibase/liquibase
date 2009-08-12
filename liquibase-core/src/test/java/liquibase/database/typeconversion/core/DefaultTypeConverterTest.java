package liquibase.database.typeconversion.core;

import org.junit.Test;
import liquibase.change.ColumnConfig;
import liquibase.database.typeconversion.TypeConverter;
import static org.junit.Assert.*;

public class DefaultTypeConverterTest {

    @Test
    public void getColumnType() {
        ColumnConfig column = new ColumnConfig();

        TypeConverter typeConverter = new  DefaultTypeConverter();
        column.setType("boolean");
        assertEquals(typeConverter.getBooleanType().getDataTypeName(), typeConverter.getColumnType(column));
        column.setType("BooLean");
        assertEquals(typeConverter.getBooleanType().getDataTypeName(), typeConverter.getColumnType(column));


        column.setType("currency");
        assertEquals(typeConverter.getCurrencyType().getDataTypeName(), typeConverter.getColumnType(column));
        column.setType("currEncy");
        assertEquals(typeConverter.getCurrencyType().getDataTypeName(), typeConverter.getColumnType(column));

        column.setType("uuid");
        assertEquals(typeConverter.getUUIDType().getDataTypeName(), typeConverter.getColumnType(column));
        column.setType("UUID");
        assertEquals(typeConverter.getUUIDType().getDataTypeName(), typeConverter.getColumnType(column));

        column.setType("blob");
        assertEquals(typeConverter.getBlobType().getDataTypeName(), typeConverter.getColumnType(column));
        column.setType("BLOB");
        assertEquals(typeConverter.getBlobType().getDataTypeName(), typeConverter.getColumnType(column));

        column.setType("clob");
        assertEquals(typeConverter.getClobType().getDataTypeName(), typeConverter.getColumnType(column));
        column.setType("CLOB");
        assertEquals(typeConverter.getClobType().getDataTypeName(), typeConverter.getColumnType(column));

        column.setType("SomethingElse");
        assertEquals("SomethingElse", typeConverter.getColumnType(column));
    }
}
