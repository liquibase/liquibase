package liquibase.database.typeconversion.core;

import org.junit.Test;
import liquibase.change.ColumnConfig;
import liquibase.database.typeconversion.TypeConverter;
import liquibase.database.structure.type.DataType;
import static org.junit.Assert.*;

public class DefaultTypeConverterTest {

    @Test
    public void getColumnType() {
        ColumnConfig column = new ColumnConfig();

        TypeConverter typeConverter = new  DefaultTypeConverter();
        column.setType("boolean");
        assertEquals(typeConverter.getBooleanType().getDataTypeName(), typeConverter.getDataType(column).toString());
        column.setType("BooLean");
        assertEquals(typeConverter.getBooleanType().getDataTypeName(), typeConverter.getDataType(column).toString());


        column.setType("currency");
        assertEquals(typeConverter.getCurrencyType().getDataTypeName(), typeConverter.getDataType(column).toString());
        column.setType("currEncy");
        assertEquals(typeConverter.getCurrencyType().getDataTypeName(), typeConverter.getDataType(column).toString());

        column.setType("uuid");
        assertEquals(typeConverter.getUUIDType().getDataTypeName(), typeConverter.getDataType(column).toString());
        column.setType("UUID");
        assertEquals(typeConverter.getUUIDType().getDataTypeName(), typeConverter.getDataType(column).toString());

        column.setType("blob");
        assertEquals(typeConverter.getBlobType().getDataTypeName(), typeConverter.getDataType(column).toString());
        column.setType("BLOB");
        assertEquals(typeConverter.getBlobType().getDataTypeName(), typeConverter.getDataType(column).toString());

        column.setType("clob");
        assertEquals(typeConverter.getClobType().getDataTypeName(), typeConverter.getDataType(column).toString());
        column.setType("CLOB");
        assertEquals(typeConverter.getClobType().getDataTypeName(), typeConverter.getDataType(column).toString());

        column.setType("SomethingElse");
        assertEquals("SomethingElse", typeConverter.getDataType(column).toString());
    }
}
