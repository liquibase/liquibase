package liquibase.change;

import static org.junit.Assert.*;

import liquibase.serializer.LiquibaseSerializable;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.structure.core.*;
import org.junit.Test;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.Date;

public class ColumnConfigTest {

    @Test
    public void constructor_everythingSet() {
        Table table = new Table();

        table.setPrimaryKey(new PrimaryKey().addColumnName(0, "colName").setName("pk_name").setTablespace("pk_tablespace"));
        table.getUniqueConstraints().add(new UniqueConstraint().setName("uq1").addColumn(0, "otherCol"));
        table.getUniqueConstraints().add(new UniqueConstraint().setName("uq2").addColumn(0, "colName"));

        table.getOutgoingForeignKeys().add(new ForeignKey().setName("fk1").setForeignKeyColumns("otherCol"));
        table.getOutgoingForeignKeys().add(new ForeignKey().setName("fk2").setForeignKeyColumns("colName").setPrimaryKeyTable(new Table().setName("otherTable")).setPrimaryKeyColumns("id"));

        Column column = new Column();
        column.setName("colName");
        column.setRelation(table);
        column.setAutoIncrementInformation(new Column.AutoIncrementInformation(3, 5));
        column.setType(new DataType("BIGINT"));
        column.setNullable(false);
        column.setDefaultValue(123);
        column.setRemarks("A Test Column");

        ColumnConfig config = new ColumnConfig(column);

        assertEquals("colName", config.getName());
        assertEquals("123", config.getDefaultValue());
        assertEquals("A Test Column", config.getRemarks());
        assertEquals("BIGINT", config.getType());
        assertEquals(false, config.getConstraints().isNullable());

        assertEquals(true, config.getConstraints().isUnique());
        assertEquals("uq2", config.getConstraints().getUniqueConstraintName());

        assertEquals(true, config.getConstraints().isPrimaryKey());
        assertEquals("pk_name", config.getConstraints().getPrimaryKeyName());
        assertEquals("pk_tablespace", config.getConstraints().getPrimaryKeyTablespace());

        assertEquals("fk2", config.getConstraints().getForeignKeyName());
        assertEquals("otherTable(id)", config.getConstraints().getReferences());

        assertEquals(true, config.isAutoIncrement());
        assertEquals(3, config.getStartWith().longValue());
        assertEquals(5, config.getIncrementBy().longValue());
    }

    @Test
    public void constructor_nothingSet() {
        Table table = new Table();

        Column column = new Column();
        column.setName("colName");
        column.setRelation(table);
        column.setType(new DataType("BIGINT"));

        ColumnConfig config = new ColumnConfig(column);

        assertEquals("colName", config.getName());
        assertNull(config.getDefaultValue());
        assertNull(config.getRemarks());
        assertEquals("BIGINT", config.getType());
        assertNull(config.getConstraints().isNullable()); //nullable could be unknown

        assertEquals(false, config.getConstraints().isUnique()); //we know it is unique or not, cannot return null
        assertNull(config.getConstraints().getUniqueConstraintName());

        assertEquals(false, config.getConstraints().isPrimaryKey()); //we know it is unique or not, cannot return null
        assertNull(config.getConstraints().getPrimaryKeyName());
        assertNull(config.getConstraints().getPrimaryKeyTablespace());

        assertNull(config.getConstraints().getForeignKeyName());
        assertNull(config.getConstraints().getReferences());

        assertEquals(false, config.isAutoIncrement());  //we know it is unique or not, cannot return null
        assertNull(config.getStartWith());
        assertNull(config.getIncrementBy());
    }

    @Test
    public void constructor_view() {
        View view = new View();

        Column column = new Column();
        column.setName("colName");
        column.setRelation(view);
        column.setType(new DataType("BIGINT"));

        ColumnConfig config = new ColumnConfig(column);

        assertEquals("colName", config.getName());
        assertEquals("BIGINT", config.getType());

        assertNull(config.getConstraints()); //return null constraints for views

        assertNull(config.isAutoIncrement());  //set to null for views
    }

    @Test
    public void setValue() throws Exception {
        assertNull(new ColumnConfig().setValue(null).getValue());
        assertEquals("abc", new ColumnConfig().setValue("abc").getValue());
        assertEquals("empty strings are saved", "", new ColumnConfig().setValue("").getValue());
        assertEquals("strings should not be trimmed", "  not trimmed  ", new ColumnConfig().setValue("  not trimmed  ").getValue());
        assertEquals("null", new ColumnConfig().setValue("null").getValue());
    }

    @Test
    public void setValueNumeric() {
        assertEquals(3, new ColumnConfig().setValueNumeric(3).getValueNumeric());
        assertEquals(3L, new ColumnConfig().setValueNumeric("3").getValueNumeric());
        assertEquals(3.5, new ColumnConfig().setValueNumeric(3.5).getValueNumeric());
        assertEquals(3.5, new ColumnConfig().setValueNumeric("3.5").getValueNumeric());
        assertEquals(-6, new ColumnConfig().setValueNumeric(-6).getValueNumeric());
        assertEquals(-6L, new ColumnConfig().setValueNumeric("-6").getValueNumeric());
        assertEquals(0, new ColumnConfig().setValueNumeric(0).getValueNumeric());
        assertEquals(0L, new ColumnConfig().setValueNumeric("0").getValueNumeric());
        assertEquals(0.33, new ColumnConfig().setValueNumeric(0.33).getValueNumeric());
        assertEquals(0.33, new ColumnConfig().setValueNumeric("0.33").getValueNumeric());
    }

    @Test
    public void setValueNumeric_null() {
        assertNull(new ColumnConfig().setValueNumeric((String) null).getValueNumeric());
        assertNull(new ColumnConfig().setValueNumeric("null").getValueNumeric());
        assertNull(new ColumnConfig().setValueNumeric("NULL").getValueNumeric());
        assertNull(new ColumnConfig().setValueNumeric((Number) null).getValueNumeric());
    }

    @Test
    public void setValueNumeric_wrapped() {
        assertEquals(52.3, new ColumnConfig().setValueNumeric("(52.3)").getValueNumeric());
        assertEquals(-32.3, new ColumnConfig().setValueNumeric("(-32.3)").getValueNumeric());

    }

    @Test
    public void setValueNumeric_function() {
        ColumnConfig columnConfig = new ColumnConfig().setValueNumeric("max_integer()");
        assertNull(columnConfig.getValueNumeric());
        assertEquals("max_integer()", columnConfig.getValueComputed().toString());

        columnConfig = new ColumnConfig().setValueNumeric("paramless_fn");
        assertNull(columnConfig.getValueNumeric());
        assertEquals("paramless_fn", columnConfig.getValueComputed().toString());

        columnConfig = new ColumnConfig().setValueNumeric("fn(3,5)");
        assertNull(columnConfig.getValueNumeric());
        assertEquals("fn(3,5)", columnConfig.getValueComputed().toString());
    }

    @Test
    public void setValueBoolean() {
        assertNull(new ColumnConfig().setValueBoolean((Boolean) null).getValueBoolean());
        assertEquals(true, new ColumnConfig().setValueBoolean(true).getValueBoolean());
        assertFalse(new ColumnConfig().setValueBoolean(false).getValueBoolean());
    }

    @Test
    public void setValueBoolean_string() {
        assertNull(new ColumnConfig().setValueBoolean("null").getValueBoolean());
        assertNull(new ColumnConfig().setValueBoolean("NULL").getValueBoolean());
        assertNull(new ColumnConfig().setValueBoolean((String) null).getValueBoolean());
        assertNull(new ColumnConfig().setValueBoolean("").getValueBoolean());
        assertNull(new ColumnConfig().setValueBoolean(" ").getValueBoolean());

        assertEquals(true, new ColumnConfig().setValueBoolean("true").getValueBoolean());
        assertEquals(true, new ColumnConfig().setValueBoolean("TRUE").getValueBoolean());
        assertEquals(true, new ColumnConfig().setValueBoolean("1").getValueBoolean());

        assertEquals(false, new ColumnConfig().setValueBoolean("false").getValueBoolean());
        assertEquals(false, new ColumnConfig().setValueBoolean("FALSE").getValueBoolean());
        assertEquals(false, new ColumnConfig().setValueBoolean("0").getValueBoolean());

        assertEquals("bool_val", new ColumnConfig().setValueBoolean("bool_val").getValueComputed().toString());
        assertEquals("2", new ColumnConfig().setValueBoolean("2").getValueComputed().toString());
    }


    @Test
    public void setValueComputed() {
        assertNull(new ColumnConfig().setValueComputed(null).getValueComputed());
        assertEquals("func", new ColumnConfig().setValueComputed(new DatabaseFunction("func")).getValueComputed().toString());
    }

    @Test
    public void setValueSequenceNext() {
        assertNull(new ColumnConfig().setValueSequenceNext(null).getValueSequenceNext());
        assertEquals("my_seq", new ColumnConfig().setValueSequenceNext(new SequenceNextValueFunction("my_seq")).getValueSequenceNext().toString());
    }

    @Test
    public void setValueDate() {
        assertNull(new ColumnConfig().setValueDate((String) null).getValueDate());
        assertNull(new ColumnConfig().setValueDate("null").getValueDate());
        assertNull(new ColumnConfig().setValueDate("NULL").getValueDate());
        assertNull(new ColumnConfig().setValueDate((Date) null).getValueDate());

        Date today = new Date();
        assertEquals(today, new ColumnConfig().setValueDate(today).getValueDate());
        assertEquals("1992-02-11 13:22:44.006", new ColumnConfig().setValueDate("1992-02-11T13:22:44.6").getValueDate().toString());
        assertEquals("1992-02-12", new ColumnConfig().setValueDate("1992-02-12").getValueDate().toString());

        assertEquals("date_func", new ColumnConfig().setValueDate("date_func").getValueComputed().toString());
    }

    @Test
    public void getValueObject() {
        assertEquals(true, new ColumnConfig().setValueBoolean(true).getValueObject());
        assertEquals(5, new ColumnConfig().setValueNumeric(5).getValueObject());
        assertEquals("1993-02-11 13:22:44.006", new ColumnConfig().setValueDate("1993-02-11T13:22:44.006").getValueObject().toString());
        assertEquals("func", new ColumnConfig().setValueComputed(new DatabaseFunction("func")).getValueObject().toString());
        assertEquals("seq_name", new ColumnConfig().setValueSequenceNext(new SequenceNextValueFunction("seq_name")).getValueObject().toString());
        assertEquals("asdg", new ColumnConfig().setValueBlobFile("asdg").getValueObject());
        assertEquals("zxcv", new ColumnConfig().setValueClobFile("zxcv").getValueObject());
        assertEquals("A value", new ColumnConfig().setValue("A value").getValueObject());
        assertNull(new ColumnConfig().getValueObject());
    }

    @Test
    public void setDefaultValueNumeric() throws ParseException {
        assertEquals(3, new ColumnConfig().setDefaultValueNumeric(3).getDefaultValueNumeric());
        assertEquals(3L, new ColumnConfig().setDefaultValueNumeric("3").getDefaultValueNumeric());
        assertEquals(3.5, new ColumnConfig().setDefaultValueNumeric(3.5).getDefaultValueNumeric());
        assertEquals(3.5, new ColumnConfig().setDefaultValueNumeric("3.5").getDefaultValueNumeric());
        assertEquals(-6, new ColumnConfig().setDefaultValueNumeric(-6).getDefaultValueNumeric());
        assertEquals(-6L, new ColumnConfig().setDefaultValueNumeric("-6").getDefaultValueNumeric());
        assertEquals(0, new ColumnConfig().setDefaultValueNumeric(0).getDefaultValueNumeric());
        assertEquals(0L, new ColumnConfig().setDefaultValueNumeric("0").getDefaultValueNumeric());
        assertEquals(0.33, new ColumnConfig().setDefaultValueNumeric(0.33).getDefaultValueNumeric());
        assertEquals(0.33, new ColumnConfig().setDefaultValueNumeric("0.33").getDefaultValueNumeric());

        assertEquals("new_value()", new ColumnConfig().setDefaultValueNumeric("new_value()").getDefaultValueComputed().toString());
    }

    @Test
    public void setDefaultValueNumeric_null() throws ParseException {
        assertNull(new ColumnConfig().setDefaultValueNumeric((String) null).getDefaultValueNumeric());
        assertNull(new ColumnConfig().setDefaultValueNumeric("null").getDefaultValueNumeric());
        assertNull(new ColumnConfig().setDefaultValueNumeric("NULL").getDefaultValueNumeric());
        assertNull(new ColumnConfig().setDefaultValueNumeric((Number) null).getDefaultValueNumeric());
    }

    @Test
    public void setDefaultValueNumeric_generatedByDefault() throws ParseException {
        ColumnConfig config = new ColumnConfig().setDefaultValueNumeric("GENERATED_BY_DEFAULT");
        assertNull(config.getDefaultValueNumeric());
        assertTrue(config.isAutoIncrement());
    }

    @Test
    public void setDefaultNumeric_wrapped() {
        assertEquals(52.3, new ColumnConfig().setDefaultValueNumeric("(52.3)").getDefaultValueNumeric());
        assertEquals(-32.3, new ColumnConfig().setDefaultValueNumeric("(-32.3)").getDefaultValueNumeric());

    }

    @Test
    public void setDefaultValueDate() {
        assertNull(new ColumnConfig().setDefaultValueDate((String) null).getDefaultValueDate());
        assertNull(new ColumnConfig().setDefaultValueDate("null").getDefaultValueDate());
        assertNull(new ColumnConfig().setDefaultValueDate("NULL").getDefaultValueDate());
        assertNull(new ColumnConfig().setDefaultValueDate((Date) null).getDefaultValueDate());
        assertNull(new ColumnConfig().setDefaultValueDate("").getDefaultValueDate());

        Date today = new Date();
        assertEquals(today, new ColumnConfig().setDefaultValueDate(today).getDefaultValueDate());
        assertEquals("1992-02-11 13:22:44.006", new ColumnConfig().setDefaultValueDate("1992-02-11T13:22:44.6").getDefaultValueDate().toString());
        assertEquals("1992-02-12", new ColumnConfig().setDefaultValueDate("1992-02-12").getDefaultValueDate().toString());

        assertEquals("date_func", new ColumnConfig().setDefaultValueDate("date_func").getDefaultValueComputed().toString());
    }

    @Test
    public void setDefaultValue() {
        assertNull(new ColumnConfig().setDefaultValue(null).getDefaultValue());
        assertEquals("abc", new ColumnConfig().setDefaultValue("abc").getDefaultValue());
        assertEquals("  abc  ", new ColumnConfig().setDefaultValue("  abc  ").getDefaultValue());
        assertEquals("null", new ColumnConfig().setDefaultValue("null").getDefaultValue());
        assertEquals("", new ColumnConfig().setDefaultValue("").getDefaultValue());
    }


    @Test
    public void setDefaultValueBoolean() {
        assertNull(new ColumnConfig().setDefaultValueBoolean((Boolean) null).getDefaultValueBoolean());
        assertEquals(true, new ColumnConfig().setDefaultValueBoolean(true).getDefaultValueBoolean());
        assertFalse(new ColumnConfig().setDefaultValueBoolean(false).getDefaultValueBoolean());
    }

    @Test
    public void setDefaultValueBoolean_string() {
        assertNull(new ColumnConfig().setDefaultValueBoolean("null").getDefaultValueBoolean());
        assertNull(new ColumnConfig().setDefaultValueBoolean("NULL").getDefaultValueBoolean());
        assertNull(new ColumnConfig().setDefaultValueBoolean((String) null).getDefaultValueBoolean());
        assertNull(new ColumnConfig().setDefaultValueBoolean("").getDefaultValueBoolean());
        assertNull(new ColumnConfig().setDefaultValueBoolean(" ").getDefaultValueBoolean());

        assertEquals(true, new ColumnConfig().setDefaultValueBoolean("true").getDefaultValueBoolean());
        assertEquals(true, new ColumnConfig().setDefaultValueBoolean("TRUE").getDefaultValueBoolean());
        assertEquals(true, new ColumnConfig().setDefaultValueBoolean("1").getDefaultValueBoolean());

        assertEquals(false, new ColumnConfig().setDefaultValueBoolean("false").getDefaultValueBoolean());
        assertEquals(false, new ColumnConfig().setDefaultValueBoolean("FALSE").getDefaultValueBoolean());
        assertEquals(false, new ColumnConfig().setDefaultValueBoolean("0").getDefaultValueBoolean());

        assertEquals("bool_val", new ColumnConfig().setDefaultValueBoolean("bool_val").getDefaultValueComputed().toString());
        assertEquals("2", new ColumnConfig().setDefaultValueBoolean("2").getDefaultValueComputed().toString());
    }

    @Test
    public void setDefaultValueComputed() {
        assertNull(new ColumnConfig().setDefaultValueComputed(null).getDefaultValueComputed());
        assertEquals("func", new ColumnConfig().setDefaultValueComputed(new DatabaseFunction("func")).getDefaultValueComputed().toString());
    }

    @Test
    public void getDefaultValueObject() {
        assertEquals(true, new ColumnConfig().setDefaultValueBoolean(true).getDefaultValueObject());
        assertEquals(5, new ColumnConfig().setDefaultValueNumeric(5).getDefaultValueObject());
        assertEquals("1993-02-11 13:22:44.006", new ColumnConfig().setDefaultValueDate("1993-02-11T13:22:44.006").getDefaultValueObject().toString());
        assertEquals("func", new ColumnConfig().setDefaultValueComputed(new DatabaseFunction("func")).getDefaultValueObject().toString());
        assertEquals("A value", new ColumnConfig().setDefaultValue("A value").getDefaultValueObject());
        assertNull(new ColumnConfig().getDefaultValueObject());
    }

    @Test
    public void setConstraints() {
        assertNull(new ColumnConfig().setConstraints(null).getConstraints());
        assertNotNull(new ColumnConfig().setConstraints(new ConstraintsConfig()).getConstraints());
    }

    @Test
    public void setAutoIncrement() {
        assertNull(new ColumnConfig().setAutoIncrement(null).isAutoIncrement());
        assertEquals(true, new ColumnConfig().setAutoIncrement(true).isAutoIncrement());
        assertFalse(new ColumnConfig().setAutoIncrement(false).isAutoIncrement());
    }

    @Test
    public void setStartWith() {
        assertNull(new ColumnConfig().setStartWith(null).getStartWith());
        assertEquals("125", new ColumnConfig().setStartWith(new BigInteger("125")).getStartWith().toString());
    }

    @Test
    public void setIncrementBy() {
        assertNull(new ColumnConfig().setIncrementBy(null).getIncrementBy());
        assertEquals("131", new ColumnConfig().setIncrementBy(new BigInteger("131")).getIncrementBy().toString());
    }

    @Test
    public void hasDefaultValue() {
        assertTrue(new ColumnConfig().setDefaultValueBoolean(true).hasDefaultValue());
        assertTrue(new ColumnConfig().setDefaultValueNumeric(5).hasDefaultValue());
        assertTrue(new ColumnConfig().setDefaultValueDate("1993-02-11T13:22:44.006").hasDefaultValue());
        assertTrue(new ColumnConfig().setDefaultValueComputed(new DatabaseFunction("func")).hasDefaultValue());
        assertTrue(new ColumnConfig().setDefaultValue("A value").hasDefaultValue());
        assertFalse(new ColumnConfig().hasDefaultValue());
    }

    @Test
    public void setRemarks() {
        assertNull(new ColumnConfig().setRemarks(null).getRemarks());
        assertEquals("yyy", new ColumnConfig().setRemarks("yyy").getRemarks());
    }

    @Test
    public void setValueClob() {
        assertNull(new ColumnConfig().setValueClobFile(null).getValueClobFile());
        assertEquals("clob_file", new ColumnConfig().setValueClobFile("clob_file").getValueClobFile());
    }

    @Test
    public void setValueBlob() {
        assertNull(new ColumnConfig().setValueBlobFile(null).getValueBlobFile());
        assertEquals("blob_file", new ColumnConfig().setValueBlobFile("blob_file").getValueBlobFile());
    }

    @Test
    public void getFieldSerializationType() {
        assertEquals(LiquibaseSerializable.SerializationType.NAMED_FIELD, new ColumnConfig().getSerializableFieldType("anythiny"));
    }

    @Test
    public void getSerializedObjectName() {
        assertEquals("column", new ColumnConfig().getSerializedObjectName());
    }
}
