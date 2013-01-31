package liquibase.change;

import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import org.junit.Test;

import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.mock;

public class ChangeMetaDataTest {

    @Test
    public void constructor() {
        HashMap<String, ChangeParameterMetaData> params = new HashMap<String, ChangeParameterMetaData>();
        params.put("a", mock(ChangeParameterMetaData.class));
        Class<? extends DatabaseObject>[] appliesTo = new Class[]{Table.class, Column.class};
        ChangeMetaData metaData = new ChangeMetaData("x", "y", 10, appliesTo, params);

        assertEquals("x", metaData.getName());
        assertEquals("y", metaData.getDescription());
        assertEquals(10, metaData.getPriority());

        assertEquals(2, metaData.getAppliesTo().length);

        assertEquals(1, metaData.getParameters().size());
        assertEquals("a", metaData.getParameters().keySet().iterator().next());
    }

    @Test
    public void constructor_nullParams() {
        ChangeMetaData metaData = new ChangeMetaData("x", "y", 5, null, null);
        assertEquals(0, metaData.getParameters().size());
    }

    @Test
    public void constructor_nullAppliesTo() {
        ChangeMetaData metaData = new ChangeMetaData("x", "y", 5, null, null);
        assertNull(metaData.getAppliesTo());
    }

    @Test
    public void constructor_emptyAppliesTo() {
        ChangeMetaData metaData = new ChangeMetaData("x", "y", 5, new Class[0], null);
        assertNull("Empty appliesTo should convert to a null appliesTo", metaData.getAppliesTo());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getParameters() {
        new ChangeMetaData("x", "y", 1, null, new HashMap()).getParameters().put("new", mock(ChangeParameterMetaData.class));
    }
}
