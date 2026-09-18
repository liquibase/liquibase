package liquibase.structure.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class GrantTest {

    @Test
    public void getName_synthesizesReadableIdentifier() {
        Grant grant = new Grant("mydb", "public", "TABLE", "orders", "SELECT", "app_user", false);

        assertEquals("SELECT ON TABLE orders TO app_user", grant.getName());
    }

    @Test
    public void getName_includesGrantOptionWhenGrantable() {
        Grant grant = new Grant("mydb", "public", "TABLE", "orders", "SELECT", "app_user", true);

        assertEquals("SELECT ON TABLE orders TO app_user WITH GRANT OPTION", grant.getName());
    }

    @Test
    public void snapshotByDefault_isFalse() {
        assertFalse(new Grant().snapshotByDefault());
    }

    @Test
    public void equals_sameAttributes_areEqual() {
        Grant grant1 = new Grant("mydb", "public", "TABLE", "orders", "SELECT", "app_user", false);
        Grant grant2 = new Grant("mydb", "public", "TABLE", "orders", "SELECT", "app_user", false);

        assertTrue(grant1.equals(grant2));
        assertEquals(grant1.hashCode(), grant2.hashCode());
    }

    @Test
    public void equals_ignoresGrantableFlag() {
        //the grant option is a mutable property of an otherwise identical grant, not part of its identity
        Grant grant1 = new Grant("mydb", "public", "TABLE", "orders", "SELECT", "app_user", false);
        Grant grant2 = new Grant("mydb", "public", "TABLE", "orders", "SELECT", "app_user", true);

        assertTrue(grant1.equals(grant2));
    }

    @Test
    public void equals_differentPrivilege_areNotEqual() {
        Grant grant1 = new Grant("mydb", "public", "TABLE", "orders", "SELECT", "app_user", false);
        Grant grant2 = new Grant("mydb", "public", "TABLE", "orders", "INSERT", "app_user", false);

        assertNotEquals(grant1, grant2);
    }

    @Test
    public void equals_differentGrantee_areNotEqual() {
        Grant grant1 = new Grant("mydb", "public", "TABLE", "orders", "SELECT", "app_user", false);
        Grant grant2 = new Grant("mydb", "public", "TABLE", "orders", "SELECT", "other_user", false);

        assertNotEquals(grant1, grant2);
    }

    @Test
    public void equals_differentObjectName_areNotEqual() {
        Grant grant1 = new Grant("mydb", "public", "TABLE", "orders", "SELECT", "app_user", false);
        Grant grant2 = new Grant("mydb", "public", "TABLE", "customers", "SELECT", "app_user", false);

        assertNotEquals(grant1, grant2);
    }
}
