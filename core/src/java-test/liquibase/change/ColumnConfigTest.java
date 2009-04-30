package liquibase.change;

import static org.junit.Assert.*;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Tests for {@link ColumnConfig}
 */
public class ColumnConfigTest {

    @Test
    public void setValue() throws Exception {
        ColumnConfig column = new ColumnConfig();

        column.setValue(null);
        assertNull(column.getValue());

        column.setValue("abc");
        assertEquals("abc", column.getValue());

        column.setValue(null);
        assertEquals("passed null should override the value", null, column.getValue());
        
        column.setValue("");
        assertEquals("passed empty strings should override the value", "", column.getValue());

    }
}
