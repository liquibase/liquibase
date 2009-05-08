package liquibase.parser.string;

import liquibase.change.AddColumnChange;
import liquibase.change.ColumnConfig;
import liquibase.change.SQLFileChange;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class StringChangeLogSerializerTest {
    
    @Test
    public void serialized_AddColumnChange() {
        AddColumnChange change = new AddColumnChange();

        assertEquals("addColumn:[\n" +
                "    columns=[]\n" +
                "]", new StringChangeLogSerializer().serialize(change));

        change.setTableName("TABLE_NAME");

        assertEquals("addColumn:[\n" +
                "    columns=[]\n" +
                "    tableName=\"TABLE_NAME\"\n" +
                "]", new StringChangeLogSerializer().serialize(change));

        change.setSchemaName("SCHEMA_NAME");
        assertEquals("addColumn:[\n" +
                "    columns=[]\n" +
                "    schemaName=\"SCHEMA_NAME\"\n" +
                "    tableName=\"TABLE_NAME\"\n" +
                "]", new StringChangeLogSerializer().serialize(change));

        ColumnConfig column = new ColumnConfig();
        change.addColumn(column);
        column.setName("COLUMN_NAME");

        assertEquals("addColumn:[\n" +
                "    columns=[\n" +
                "        column:[\n" +
                "            name=\"COLUMN_NAME\"\n" +
                "        ]\n" +
                "    ]\n" +
                "    schemaName=\"SCHEMA_NAME\"\n" +
                "    tableName=\"TABLE_NAME\"\n" +
                "]", new StringChangeLogSerializer().serialize(change));

        ColumnConfig column2 = new ColumnConfig();
        change.addColumn(column2);
        column2.setName("COLUMN2_NAME");
        column2.setAutoIncrement(true);
        column2.setValueNumeric(52);

        assertEquals("addColumn:[\n" +
                "    columns=[\n" +
                "        column:[\n" +
                "            name=\"COLUMN_NAME\"\n" +
                "        ],\n" +
                "        column:[\n" +
                "            autoIncrement=\"true\"\n" +
                "            name=\"COLUMN2_NAME\"\n" +
                "            valueNumeric=\"52\"\n" +
                "        ]\n" +
                "    ]\n" +
                "    schemaName=\"SCHEMA_NAME\"\n" +
                "    tableName=\"TABLE_NAME\"\n" +
                "]", new StringChangeLogSerializer().serialize(change));
    }

    @Test
    public void serialized_SQLFileChange() {
        SQLFileChange change = new SQLFileChange();

        assertEquals("sqlFile:[]", new StringChangeLogSerializer().serialize(change));

        change.setPath("PATH/TO/File.txt");

        assertEquals("sqlFile:[\n" +
                "    path=\"PATH/TO/File.txt\"\n" +
                "]", new StringChangeLogSerializer().serialize(change));
    }


    @Test
    public void tryAllChangeCombinations() {
        AddColumnChange change = new AddColumnChange();
    }

}
