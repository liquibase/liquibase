package liquibase.serializer.core.string;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import liquibase.change.*;
import liquibase.change.DatabaseChangeProperty;
import liquibase.change.core.*;
import liquibase.change.custom.CustomChangeWrapper;
import liquibase.change.custom.CustomSqlChange;
import liquibase.change.custom.ExampleCustomSqlChange;
import liquibase.changelog.ChangeLogParameters;
import liquibase.logging.Logger;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.DatabaseFunction;

import liquibase.statement.SequenceCurrentValueFunction;
import liquibase.statement.SequenceNextValueFunction;
import org.junit.Test;

public class StringChangeLogSerializerTest {

    @Test
    public void serialized_CustomChange() throws Exception {

        String expectedString = "customChange:[\n" +
                "    class=\"liquibase.change.custom.ExampleCustomSqlChange\"\n" +
                "    param={\n" +
                "        columnName=\"column_name\",\n" +
                "        newValue=\"new_value\",\n" +
                "        tableName=\"table_name\"\n" +
                "    }\n" +
                "]";

        CustomChangeWrapper wrapper = new CustomChangeWrapper();
        wrapper.setResourceAccessor(new ClassLoaderResourceAccessor());
        //wrapper.setFileOpener(new JUnitResourceAccessor());
        //wrapper.setClassLoader(new JUnitResourceAccessor().toClassLoader());
        wrapper.setClassLoader(getClass().getClassLoader());
        wrapper.setClass("liquibase.change.custom.ExampleCustomSqlChange");
        wrapper.setParam("columnName", "column_name");
        wrapper.setParam("newValue", "new_value");
        wrapper.setParam("tableName", "table_name");

        assertEquals(expectedString, new StringChangeLogSerializer().serialize(wrapper,false));
    }

    @Test
    public void serialized_AddColumnChange() {
        AddColumnChange change = new AddColumnChange();

        assertEquals("addColumn:[\n" +
                "    columns=[]\n" +
                "]", new StringChangeLogSerializer().serialize(change, false));

        change.setTableName("TABLE_NAME");

        assertEquals("addColumn:[\n" +
                "    columns=[]\n" +
                "    tableName=\"TABLE_NAME\"\n" +
                "]", new StringChangeLogSerializer().serialize(change, false));

        change.setSchemaName("SCHEMA_NAME");
        assertEquals("addColumn:[\n" +
                "    columns=[]\n" +
                "    schemaName=\"SCHEMA_NAME\"\n" +
                "    tableName=\"TABLE_NAME\"\n" +
                "]", new StringChangeLogSerializer().serialize(change, false));

        AddColumnConfig column = new AddColumnConfig();
        change.addColumn(column);
        column.setName("COLUMN_NAME");

        assertEquals("addColumn:[\n" +
                "    columns=[\n" +
                "        [\n" +
                "            name=\"COLUMN_NAME\"\n" +
                "        ]\n" +
                "    ]\n" +
                "    schemaName=\"SCHEMA_NAME\"\n" +
                "    tableName=\"TABLE_NAME\"\n" +
                "]", new StringChangeLogSerializer().serialize(change, false));

        AddColumnConfig column2 = new AddColumnConfig();
        change.addColumn(column2);
        column2.setName("COLUMN2_NAME");
        column2.setAutoIncrement(true);
        column2.setValueNumeric(52);

        assertEquals("addColumn:[\n" +
                "    columns=[\n" +
                "        [\n" +
                "            name=\"COLUMN_NAME\"\n" +
                "        ],\n" +
                "        [\n" +
                "            autoIncrement=\"true\"\n" +
                "            name=\"COLUMN2_NAME\"\n" +
                "            valueNumeric=\"52\"\n" +
                "        ]\n" +
                "    ]\n" +
                "    schemaName=\"SCHEMA_NAME\"\n" +
                "    tableName=\"TABLE_NAME\"\n" +
                "]", new StringChangeLogSerializer().serialize(change, false));
    }

    @Test
    public void serialized_AddForeignKeyConstraint() {
        AddForeignKeyConstraintChange change = new AddForeignKeyConstraintChange();

        assertEquals("addForeignKeyConstraint:[]", new StringChangeLogSerializer().serialize(change, false));

        change.setBaseTableName("TABLE_NAME");
        change.setBaseColumnNames("COL1, COL2");
        change.setBaseTableSchemaName("BASE_SCHEM");
        change.setConstraintName("FK_TEST");
        change.setDeferrable(true);
        change.setInitiallyDeferred(true);
        change.setDeleteCascade(true);
        change.setOnDelete("SET NULL");
        change.setOnUpdate("NO ACTION");
        change.setReferencedTableName("REF_TABLE");
        change.setReferencedColumnNames("COLA, COLB");
        change.setReferencedTableSchemaName("REF_SCHEM");

        assertEquals("addForeignKeyConstraint:[\n" +
                "    baseColumnNames=\"COL1, COL2\"\n" +
                "    baseTableName=\"TABLE_NAME\"\n" +
                "    baseTableSchemaName=\"BASE_SCHEM\"\n" +
                "    constraintName=\"FK_TEST\"\n" +
                "    deferrable=\"true\"\n" +
                "    initiallyDeferred=\"true\"\n" +
                "    onDelete=\"SET NULL\"\n" +
                "    onUpdate=\"NO ACTION\"\n" +
                "    referencedColumnNames=\"COLA, COLB\"\n" +
                "    referencedTableName=\"REF_TABLE\"\n" +
                "    referencedTableSchemaName=\"REF_SCHEM\"\n" +
                "]", new StringChangeLogSerializer().serialize(change, false));

    }
    @Test
    public void serialized_AddUniqueKeyConstraint() {
    	AddUniqueConstraintChange change = new AddUniqueConstraintChange();

        assertEquals("addUniqueConstraint:[]", new StringChangeLogSerializer().serialize(change, false));

        change.setTableName("TABLE_NAME");
        change.setColumnNames("COL1, COL2");
        change.setSchemaName("BASE_SCHEM");
        change.setConstraintName("FK_TEST");
        change.setDeferrable(true);
        change.setInitiallyDeferred(true);
        change.setDisabled(true);
        change.setTablespace("TABLESPACE1");

        assertEquals("addUniqueConstraint:[\n" +
                "    columnNames=\"COL1, COL2\"\n" +
                "    constraintName=\"FK_TEST\"\n" +
                "    deferrable=\"true\"\n" +
                "    disabled=\"true\"\n" +
                "    initiallyDeferred=\"true\"\n" +
                "    schemaName=\"BASE_SCHEM\"\n" +
                "    tableName=\"TABLE_NAME\"\n" +
                "    tablespace=\"TABLESPACE1\"\n" +
                "]", new StringChangeLogSerializer().serialize(change, false));

    }
//    @Test
//    public void serialized_changeSet() {
//        ChangeSet changeSet = new ChangeSet("1", "ted", true, false, "com/example/test.xml", "c:/com/exmple/test", "context1, context2", "mysql, oracle");
//        AddColumnChange change = new AddColumnChange();
//        changeSet.addChange(change);
//
//        assertEquals("changeSet:[\n" +
//                "    alwaysRun=\"true\"\n" +
//                "    author=\"ted\"\n" +
//                "    contextList=\"context1,context2\"\n" +
//                "    dbmsList=\"mysql,oracle\"\n" +
//                "    filePath=\"com/example/test.xml\"\n" +
//                "    id=\"1\"\n" +
//                "    physicalFilePath=\"c:/com/example/test.xml\"\n" +
//                "    runOnChange=\"false\"\n" +
//                "    changes: [\n" +
//                "        addColumn:[\n" +
//                "            columns=[]\n" +
//                "        ]\n" +
//                "    ]\n" +
//                "]", new StringChangeLogSerializer().serialize(changeSet));
//    }

    @Test
    public void serialized_SQLFileChange() {
        SQLFileChange change = new SQLFileChange();

        assertEquals("sqlFile:[\n" +
                "    splitStatements=\"true\"\n" +
                "    stripComments=\"false\"\n]", new StringChangeLogSerializer().serialize(change, false));

        change.setPath("PATH/TO/File.txt");

        assertEquals("sqlFile:[\n" +
                "    path=\"PATH/TO/File.txt\"\n" +
                "    splitStatements=\"true\"\n" +
                "    stripComments=\"false\"\n" +
                "]", new StringChangeLogSerializer().serialize(change, false));
    }

    @Test
    public void serialized_rawSql() {
        RawSQLChange change = new RawSQLChange();

        assertEquals("sql:[\n" +
                "    splitStatements=\"true\"\n" +
                "    stripComments=\"false\"\n]", new StringChangeLogSerializer().serialize(change, false));

        change.setSql("some SQL Here");

        assertEquals("sql:[\n" +
                "    splitStatements=\"true\"\n" +
                "    sql=\"some SQL Here\"\n" +
                "    stripComments=\"false\"\n" +
                "]", new StringChangeLogSerializer().serialize(change, false));
    }

    @Test
    public void tryAllChanges() throws Exception {
        for (SortedSet<Class<? extends Change>> changeClassSet : ChangeFactory.getInstance().getRegistry().values()) {
            Change change = changeClassSet.iterator().next().getConstructor().newInstance();

            setFields(change);

            String string = new StringChangeLogSerializer().serialize(change, false);
//            System.out.println(string);
//            System.out.println("-------------");
            assertTrue("@ in string.  Probably poorly serialzed object reference." + string, string.indexOf("@") < 0);
        }
    }

    private void setFields(Object object) throws Exception {
        Class clazz = object.getClass();
        if (clazz.getName().indexOf(".ext.") > 0) {
            return; //don't worry about ext samples
        }
        
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getAnnotation(DatabaseChangeProperty.class) != null && !field.getAnnotation(DatabaseChangeProperty.class).isChangeProperty()) {
                continue;
            }
            field.setAccessible(true);
            if (field.isSynthetic() || field.getType().getName().equals("[[Z")) {
                //nothing, from emma
            } else if (field.getName().equals("serialVersionUID")) {
                //nothing
            } else if (Modifier.isStatic(field.getModifiers())) {
                // nothing if it is static
            }
            else if (field.getType().equals(Logger.class)) {
                //nothing
            } else if (field.getType().equals(ResourceAccessor.class)) {
                //nothing
            } else if (field.getType().equals(ClassLoader.class)) {
                //nothing
            } else if (field.getType().equals(InputStream.class)) {
                //nothing
            } else if (field.getType().equals(long.class)) {
                field.set(object, createInteger().longValue());
            } else if (field.getType().equals(String.class)) {
                field.set(object, createString());
            } else if (field.getType().equals(Number.class)) {
                field.set(object, createNumber());
            } else if (field.getType().equals(Integer.class)) {
                field.set(object, createInteger());
            } else if (field.getType().equals(BigInteger.class)) {
                field.set(object, createBigInteger());
            } else if (field.getType().equals(Date.class)) {
                field.set(object, createDate());
            } else if (field.getType().equals(Boolean.class)) {
                field.set(object, createBoolean());
            } else if (field.getType().equals(ColumnConfig.class)) {
                field.set(object, createColumnConfig());
            } else if (field.getType().equals(SequenceNextValueFunction.class)) {
                field.set(object, createSequenceNextValueFunction());
            } else if (field.getType().equals(SequenceCurrentValueFunction.class)) {
                field.set(object, createSequenceCurrentValueFunction());
            } else if (field.getType().equals(DatabaseFunction.class)) {
                field.set(object, createDatabaseFunction());
            } else if (field.getType().equals(ConstraintsConfig.class)) {
                field.set(object, createConstraintsConfig());
            } else if (field.getType().getName().equals("liquibase.change.custom.CustomChange")) {
                field.set(object, createCustomChange());
            } else if (field.getType().equals(Map.class)) {
                field.set(object, createMap());
            } else if (field.getType().equals(ChangeLogParameters.class)){
                 // TODO: unclear what to do here ...               
            } else if (Collection.class.isAssignableFrom(field.getType())) {
                Type genericType = field.getGenericType();
                if (genericType instanceof ParameterizedType) {
                    int genericsLength = ((ParameterizedType) genericType).getActualTypeArguments().length;
                    if (genericsLength == 1) {
                        Class typeToCreate = (Class) ((ParameterizedType) genericType).getActualTypeArguments()[0];
                        Collection collection;
                        if (field.getType().equals(List.class)) {
                            collection = new ArrayList();
                        } else if (field.getType().equals(SortedSet.class)) {
                            collection = new TreeSet();
                        } else {
                            throw new RuntimeException("Unknown collection type: " + field.getType().getName());
                        }
                        if (typeToCreate.equals(ColumnConfig.class)) {
                            collection.add(createColumnConfig());
                            collection.add(createColumnConfig());
                        } else if (typeToCreate.equals(AddColumnConfig.class)) {
                            collection.add(createAddColumnConfig());
                            collection.add(createAddColumnConfig());
                        } else if (typeToCreate.equals(LoadDataColumnConfig.class)) {
                            collection.add(createLoadDataColumnConfig());
                            collection.add(createLoadDataColumnConfig());
                        } else if (typeToCreate.equals(String.class)) {
                            collection.add(createString());
                            collection.add(createString());
                        } else {
                            throw new RuntimeException("Unknown generic type for " + clazz.getName() + "." + field.getName() + ": " + typeToCreate.getName());
                        }
                        field.set(object, collection);
                    } else {
                        throw new RuntimeException("Found " + genericsLength + " generics for " + clazz.getName() + "." + field.getName());
                    }
                } else {
                    fail("List not generic");
                }
            } else {
                fail("Unknown field type in " + clazz.getName() + ": " + field.getType().getName());
            }
        }

    }

    private LoadDataColumnConfig createLoadDataColumnConfig() throws Exception {
        LoadDataColumnConfig config = new LoadDataColumnConfig();
        setFields(config);
        return config;
    }

    private String createString() {
        return Long.toString(Math.abs(new Random().nextLong()), 36);
    }

    private Number createNumber() {
        return new Random().nextDouble() * 10000;
    }

    private Integer createInteger() {
        return new Random().nextInt();
    }

    private BigInteger createBigInteger() {

        return new BigInteger(20, new Random());
    }

    private Date createDate() {
        return new Date(new Random().nextLong());
    }

    private Boolean createBoolean() {
        return true;
    }

    private Map createMap() {
        Map map = new HashMap();
        map.put(createString(), createString());
        map.put(createString(), createString());
        map.put(createString(), createString());
        return map;
    }

    private ColumnConfig createColumnConfig() throws Exception {
        ColumnConfig config = new ColumnConfig();
        setFields(config);
        return config;
    }

    private AddColumnConfig createAddColumnConfig() throws Exception {
        AddColumnConfig config = new AddColumnConfig();
        setFields(config);
        return config;
    }

    private DatabaseFunction createDatabaseFunction() throws Exception {
        DatabaseFunction function = new DatabaseFunction("FUNCTION_HERE");
        setFields(function);
        return function;
    }

    private SequenceNextValueFunction createSequenceNextValueFunction() throws Exception {
        SequenceNextValueFunction function = new SequenceNextValueFunction("Sequence1");
        setFields(function);
        return function;
    }

    private SequenceCurrentValueFunction createSequenceCurrentValueFunction() throws Exception {
        SequenceCurrentValueFunction function = new SequenceCurrentValueFunction("Sequence1");
        setFields(function);
        return function;
    }

    private ConstraintsConfig createConstraintsConfig() throws Exception {
        ConstraintsConfig config = new ConstraintsConfig();
        setFields(config);
        return config;
    }

    private CustomSqlChange createCustomChange() throws Exception {
        CustomSqlChange config = new ExampleCustomSqlChange();
        setFields(config);
        return config;
    }

    @Test
    public void serialize_withDoubleOnJava6() {
        InsertDataChange change = new InsertDataChange();
        change.setTableName("NUMBER_TABLE");
        change.addColumn(new ColumnConfig().setName("VALUE").setValueNumeric(new Double("0.001")));
        String out = new StringChangeLogSerializer().serialize(change, true);
        assertEquals("insert:[\n" +
                "    columns=[\n" +
                "        [\n" +
                "            name=\"VALUE\"\n" +
                "            valueNumeric=\"0.001\"\n" +
                "        ]\n" +
                "    ]\n" +
                "    tableName=\"NUMBER_TABLE\"\n" +
                "]", out);
    }
}
