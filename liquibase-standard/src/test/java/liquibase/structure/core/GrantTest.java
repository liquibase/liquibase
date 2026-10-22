package liquibase.structure.core;

import org.junit.Test;

import java.util.Locale;

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

    @Test
    public void equals_differentGrantor_areNotEqual() {
        //the same privilege granted to the same grantee by two different roles are two distinct grant rows
        Grant grant1 = new Grant("mydb", "public", "TABLE", "orders", "SELECT", "app_user", false, "table_owner");
        Grant grant2 = new Grant("mydb", "public", "TABLE", "orders", "SELECT", "app_user", false, "other_owner");

        assertNotEquals(grant1, grant2);
    }

    @Test
    public void equals_sameGrantor_areEqual() {
        Grant grant1 = new Grant("mydb", "public", "TABLE", "orders", "SELECT", "app_user", false, "table_owner");
        Grant grant2 = new Grant("mydb", "public", "TABLE", "orders", "SELECT", "app_user", false, "table_owner");

        assertTrue(grant1.equals(grant2));
        assertEquals(grant1.hashCode(), grant2.hashCode());
    }

    @Test
    public void getName_includesGrantorWhenPresent() {
        Grant grant = new Grant("mydb", "public", "TABLE", "orders", "SELECT", "app_user", false, "table_owner");

        assertEquals("SELECT ON TABLE orders TO app_user GRANTED BY table_owner", grant.getName());
    }

    @Test
    public void equals_caseDiffersUnderTurkishLocale_stillEqual() {
        //"I".toLowerCase() under tr-TR yields a dotless "ı", not "i" - case folding must not depend on the
        //default locale or privileges like INSERT would fail to match themselves on a Turkish-locale JVM
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));

            Grant grant1 = new Grant("mydb", "public", "TABLE", "orders", "INSERT", "app_user", false);
            Grant grant2 = new Grant("mydb", "public", "TABLE", "orders", "insert", "app_user", false);

            assertTrue(grant1.equals(grant2));
            assertEquals(grant1.hashCode(), grant2.hashCode());
        } finally {
            Locale.setDefault(original);
        }
    }
}
