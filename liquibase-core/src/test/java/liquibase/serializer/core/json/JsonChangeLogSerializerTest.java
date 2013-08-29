package liquibase.serializer.core.json;

import liquibase.change.ColumnConfig;
import liquibase.change.core.AddColumnChange;
import liquibase.changelog.ChangeSet;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceNextValueFunction;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class JsonChangeLogSerializerTest {

    @Test
    public void serialize_changeSet() {
        AddColumnChange addColumnChange = new AddColumnChange();
        addColumnChange.setCatalogName("cat");
        addColumnChange.addColumn(new ColumnConfig().setName("col1").setDefaultValueNumeric(3));
        addColumnChange.addColumn(new ColumnConfig().setName("col2").setDefaultValueComputed(new DatabaseFunction("NOW()")));
        addColumnChange.addColumn(new ColumnConfig().setName("col3").setDefaultValueBoolean(true));
        addColumnChange.addColumn(new ColumnConfig().setName("col2").setDefaultValueDate(new Date(0)));
        addColumnChange.addColumn(new ColumnConfig().setName("col2").setDefaultValueSequenceNext(new SequenceNextValueFunction("seq_me")));

        ChangeSet changeSet = new ChangeSet("1", "nvoxland", false, false, "path/to/file.json", null, null, null);
        changeSet.addChange(addColumnChange);
        assertEquals("{\n" +
                "  \"changeSet\": {\n" +
                "    \"id\": \"1\",\n" +
                "    \"author\": \"nvoxland\",\n" +
                "    \"changes\": [\n" +
                "      {\n" +
                "        \"addColumn\": {\n" +
                "          \"catalogName\": \"cat\",\n" +
                "          \"columns\": [\n" +
                "            {\n" +
                "              \"column\": {\n" +
                "                \"defaultValueNumeric\": 3,\n" +
                "                \"name\": \"col1\"\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"column\": {\n" +
                "                \"defaultValueComputed\": \"NOW()\",\n" +
                "                \"name\": \"col2\"\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"column\": {\n" +
                "                \"defaultValueBoolean\": true,\n" +
                "                \"name\": \"col3\"\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"column\": {\n" +
                "                \"defaultValueDate\": 1970-01-01T00:00:00Z,\n" +
                "                \"name\": \"col2\"\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"column\": {\n" +
                "                \"defaultValueSequenceNext\": \"seq_me\",\n" +
                "                \"name\": \"col2\"\n" +
                "              }\n" +
                "            }]\n" +
                "          \n" +
                "        }\n" +
                "      }]\n" +
                "    \n" +
                "  }\n" +
                "}\n", new JsonChangeLogSerializer().serialize(changeSet, true));
    }
}
