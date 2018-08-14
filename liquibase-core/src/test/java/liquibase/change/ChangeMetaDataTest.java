package liquibase.change;

import liquibase.database.core.H2Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.structure.core.View;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;

import static liquibase.test.Assert.assertSetsEqual;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class ChangeMetaDataTest {

    @Test
    public void constructor() {
        HashSet<ChangeParameterMetaData> params = new HashSet<ChangeParameterMetaData>();
        params.add(new ChangeParameterMetaData(new ExampleAbstractChange(), "a", "a", null, null, null, Integer.class, null, null, null, null));

        HashMap<String, String> notes = new HashMap<String, String>();
        notes.put("db1", "note1");
        notes.put("db2", "note2");

        String[] appliesTo = new String[] {"table", "column"};
        ChangeMetaData metaData = new ChangeMetaData("x", "y", 10, appliesTo, notes, params);

        assertEquals("x", metaData.getName());
        assertEquals("y", metaData.getDescription());
        assertEquals(10, metaData.getPriority());

        assertEquals(2, metaData.getAppliesTo().size());

        assertEquals(1, metaData.getParameters().size());
        assertEquals("a", metaData.getParameters().keySet().iterator().next());
        assertEquals("note1", metaData.getNotes("db1"));
        assertEquals("note2", metaData.getNotes("db2"));
        assertNull(metaData.getNotes("db3"));

    }

    @Test
    public void constructor_nullParams() {
        ChangeMetaData metaData = new ChangeMetaData("x", "y", 5, null, null, null);
        assertEquals(0, metaData.getParameters().size());
    }

    @Test
    public void constructor_nullAppliesTo() {
        ChangeMetaData metaData = new ChangeMetaData("x", "y", 5, null, null, null);
        assertNull(metaData.getAppliesTo());
    }

    @Test
    public void constructor_emptyAppliesTo() {
        ChangeMetaData metaData = new ChangeMetaData("x", "y", 5, new String[0], null, null);
        assertNull("Empty appliesTo should convert to a null appliesTo", metaData.getAppliesTo());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getParameters_unmodifyable() {
        new ChangeMetaData("x", "y", 1, null, null, new HashSet()).getParameters().put("new", mock(ChangeParameterMetaData.class));
    }

    @Test
    public void getRequiredParameters_empty() {
        ChangeMetaData changeMetaData = new ChangeMetaData("x", "y", 1, null, null, null);

        assertEquals(0, changeMetaData.getRequiredParameters(new H2Database()).size());
    }

    @Test
    public void getRequiredParameters() {
        HashSet<ChangeParameterMetaData> parameters = new HashSet<ChangeParameterMetaData>();
        parameters.add(new ChangeParameterMetaData(new ExampleAbstractChange(), "noneRequired", "x", null, null, null, Integer.class, new String[]{"none"}, null, null, null));
        parameters.add(new ChangeParameterMetaData(new ExampleAbstractChange(), "allRequired", "x", null, null, null, Integer.class, new String[]{"all"}, null, null, null));
        parameters.add(new ChangeParameterMetaData(new ExampleAbstractChange(), "h2Required", "x", null, null, null, Integer.class, new String[] {"h2"}, null, null, null));
        parameters.add(new ChangeParameterMetaData(new ExampleAbstractChange(), "oracleRequired", "x", null, null, null, Integer.class, new String[] {"oracle"}, null, null, null));
        ChangeMetaData changeMetaData = new ChangeMetaData("x", "y", 1, null, null, parameters);

        assertSetsEqual(new String[]{"allRequired", "h2Required"}, changeMetaData.getRequiredParameters(new H2Database()).keySet());
        assertSetsEqual(new String[]{"allRequired", "oracleRequired"}, changeMetaData.getRequiredParameters(new OracleDatabase()).keySet());
        assertSetsEqual(new String[]{"allRequired"}, changeMetaData.getRequiredParameters(new MySQLDatabase()).keySet());
    }

    @Test
    public void getOptionalParameters() {
        HashSet<ChangeParameterMetaData> parameters = new HashSet<ChangeParameterMetaData>();
        parameters.add(new ChangeParameterMetaData(new ExampleAbstractChange(), "noneRequired", "x", null, null, null, Integer.class, new String[]{"none"}, null, null, null));
        parameters.add(new ChangeParameterMetaData(new ExampleAbstractChange(), "allRequired", "x", null, null, null, Integer.class, new String[]{"all"}, null, null, null));
        parameters.add(new ChangeParameterMetaData(new ExampleAbstractChange(), "h2Required", "x", null, null, null, Integer.class, new String[] {"h2"}, null, null, null));
        parameters.add(new ChangeParameterMetaData(new ExampleAbstractChange(), "oracleRequired", "x", null, null, null, Integer.class, new String[] {"oracle"}, null, null, null));
        ChangeMetaData changeMetaData = new ChangeMetaData("x", "y", 1, null, null, parameters);

        assertSetsEqual(new String[]{"noneRequired", "oracleRequired"}, changeMetaData.getOptionalParameters(new H2Database()).keySet());
        assertSetsEqual(new String[]{"noneRequired", "h2Required"}, changeMetaData.getOptionalParameters(new OracleDatabase()).keySet());
        assertSetsEqual(new String[]{"noneRequired", "h2Required", "oracleRequired"}, changeMetaData.getOptionalParameters(new MySQLDatabase()).keySet());
    }

    @Test
    public void appliesTo() {
        ChangeMetaData metaData = new ChangeMetaData("x", "y", 5, new String[] {"table", "column"}, null, null);
        assertTrue(metaData.appliesTo(new Table()));
        assertTrue(metaData.appliesTo(new Column()));
        assertFalse(metaData.appliesTo(new View()));
    }

    @Test
    public void appliesTo_nullAppliesTo() {
        ChangeMetaData metaData = new ChangeMetaData("x", "y", 5, new String[0], null, null);
        assertFalse(metaData.appliesTo(new Table()));
        assertFalse(metaData.appliesTo(new Column()));
    }
}
