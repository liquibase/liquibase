package liquibase.serializer.core.json;

import liquibase.change.AddColumnConfig;
import liquibase.change.core.AddColumnChange;
import liquibase.changelog.ChangeSet;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.precondition.core.PreconditionContainer.ErrorOption;
import liquibase.precondition.core.PreconditionContainer.FailOption;
import liquibase.precondition.core.PreconditionContainer.OnSqlOutputOption;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceNextValueFunction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class JsonChangeLogSerializerTest {

    private String origTimeZone =  System.getProperty("user.timezone");

    @Before
    public void setTimeZoneToUTC() {
        System.setProperty("user.timezone", "UTC");
    }

    @Test
    public void serialize_changeSet() {
        //given
        AddColumnChange addColumnChange = new AddColumnChange();
        addColumnChange.setCatalogName("cat");
        addColumnChange.addColumn((AddColumnConfig) new AddColumnConfig().setName("col1").setDefaultValueNumeric(3));
        addColumnChange.addColumn((AddColumnConfig) new AddColumnConfig().setName("col2").setDefaultValueComputed(new DatabaseFunction("NOW()")));
        addColumnChange.addColumn((AddColumnConfig) new AddColumnConfig().setName("col3").setDefaultValueBoolean(true));
        addColumnChange.addColumn((AddColumnConfig) new AddColumnConfig().setName("col2").setDefaultValueDate(new Date(0)));
        addColumnChange.addColumn((AddColumnConfig) new AddColumnConfig().setName("col2").setDefaultValueSequenceNext(new SequenceNextValueFunction("seq_me")));
        ChangeSet changeSet = new ChangeSet("1", "nvoxland", false, false, "path/to/file.json", null, null, null);
        changeSet.setPreconditions(newSamplePreconditions());
        changeSet.addChange(addColumnChange);
        //when
        String serializedJson = new JsonChangeLogSerializer().serialize(changeSet, true);
        //then
        assertEquals("{\n" +
                "  \"changeSet\": {\n" +
                "    \"id\": \"1\",\n" +
                "    \"author\": \"nvoxland\",\n" +
                "    \"objectQuotingStrategy\": \"LEGACY\",\n" +
                "    \"preconditions\": {\n" +
                "      \"preConditions\": {\n" +
                "        \"nestedPreconditions\": [\n" +
                "          {\n" +
                "            \"preConditions\": {\n" +
                "              \"onError\": \"WARN\",\n" +
                "              \"onFail\": \"CONTINUE\",\n" +
                "              \"onSqlOutput\": \"TEST\"\n" +
                "            }\n" +
                "          }]\n" +
                "        ,\n" +
                "        \"onError\": \"CONTINUE\",\n" +
                "        \"onFail\": \"MARK_RAN\",\n" +
                "        \"onSqlOutput\": \"FAIL\"\n" +
                "      }\n" +
                "    },\n" +
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
                "                \"defaultValueDate\": \"1970-01-01T00:00:00\",\n" +
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
                "}\n", serializedJson);
    }

    private PreconditionContainer newSamplePreconditions() {
        PreconditionContainer precondition = new PreconditionContainer();
        precondition.setOnError(ErrorOption.CONTINUE);
        precondition.setOnFail(FailOption.MARK_RAN);
        precondition.setOnSqlOutput(OnSqlOutputOption.FAIL);
        PreconditionContainer nestedPrecondition = new PreconditionContainer();
        nestedPrecondition.setOnError(ErrorOption.WARN);
        nestedPrecondition.setOnFail(FailOption.CONTINUE);
        nestedPrecondition.setOnSqlOutput(OnSqlOutputOption.TEST);
        precondition.addNestedPrecondition(nestedPrecondition);
        return precondition;
    }

    @After
    public void setTimeZoneBackToDefault() {
        System.setProperty("user.timezone", origTimeZone);
    }
}
