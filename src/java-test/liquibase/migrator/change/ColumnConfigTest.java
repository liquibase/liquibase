package liquibase.migrator.change;

import junit.framework.TestCase;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

public class ColumnConfigTest extends TestCase {

    public void testSetValue() throws Exception {
        ColumnConfig column = new ColumnConfig();

        column.setValue(null);
        assertEquals(null, column.getValue());

        column.setValue("abc");
        assertEquals("abc", column.getValue());

        column.setValue("");
        assertEquals("passed empty strings don't override the value", "abc", column.getValue());

        column.setValue(null);
        assertEquals("passed null doesn't override the value", "abc", column.getValue());

    }

    public void testCreateNode() throws Exception {
        ColumnConfig column = new ColumnConfig();
        column.setName("id");
        column.setType("varchar(255)");
        column.setDefaultValue("test Value");
        column.setValue("some value here");

        ConstraintsConfig constraints = new ConstraintsConfig();
        constraints.setDeferrable(true);
        constraints.setDeleteCascade(true);
        constraints.setForeignKeyName("FK_NAME");
        constraints.setInitiallyDeferred(true);
        constraints.setNullable(false);
        constraints.setPrimaryKey(true);
        constraints.setReferences("state(id)");
        constraints.setUnique(true);
        column.setConstraints(constraints);

        Element element = column.createNode(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
        assertEquals("column", element.getTagName());
        assertEquals("id", element.getAttribute("name"));
        assertEquals("varchar(255)", element.getAttribute("type"));
        assertEquals("test Value", element.getAttribute("defaultValue"));
        assertEquals("some value here", element.getAttribute("value"));

        Element constraintsElement = (Element) element.getChildNodes().item(0);
        assertEquals(8, constraintsElement.getAttributes().getLength());
        assertEquals("true", constraintsElement.getAttribute("deferrable"));
        assertEquals("true", constraintsElement.getAttribute("deleteCascade"));
        assertEquals("FK_NAME", constraintsElement.getAttribute("foreignKeyName"));
        assertEquals("true", constraintsElement.getAttribute("initiallyDeferred"));
        assertEquals("false", constraintsElement.getAttribute("nullable"));
        assertEquals("true", constraintsElement.getAttribute("primaryKey"));
        assertEquals("state(id)", constraintsElement.getAttribute("references"));
        assertEquals("true", constraintsElement.getAttribute("unique"));

    }
}