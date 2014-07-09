package liquibase.structure.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PrimaryKeyTest  {

    @Test
    public void setColumn_singlePKColumn() {
        PrimaryKey pk = new PrimaryKey();
        pk.addColumnName(0, "id");

        assertEquals(1, pk.getColumnNamesAsList().size());
    }

    @Test
    public void setColumn_outOfOrder() {
        PrimaryKey pk = new PrimaryKey();
        pk.addColumnName(1, "id2");
        pk.addColumnName(0, "id1");

        assertEquals(2, pk.getColumnNamesAsList().size());
        assertEquals("id1", pk.getColumnNamesAsList().get(0));
        assertEquals("id2", pk.getColumnNamesAsList().get(1));
    }

    @Test
    public void setColumn_inOrder() {
        PrimaryKey pk = new PrimaryKey();
        pk.addColumnName(0, "id1");
        pk.addColumnName(1, "id2");

        assertEquals(2, pk.getColumnNamesAsList().size());
        assertEquals("id1", pk.getColumnNamesAsList().get(0));
        assertEquals("id2", pk.getColumnNamesAsList().get(1));
    }
}
