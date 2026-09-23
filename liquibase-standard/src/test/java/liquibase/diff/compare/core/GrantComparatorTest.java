package liquibase.diff.compare.core;

import liquibase.database.core.PostgresDatabase;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.structure.core.Grant;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GrantComparatorTest {

    private final GrantComparator comparator = new GrantComparator();
    private final PostgresDatabase database = new PostgresDatabase();
    private final DatabaseObjectComparatorChain chain =
            new DatabaseObjectComparatorChain(Collections.emptyList(), new CompareControl.SchemaComparison[0]);

    @Test
    public void getPriority_grant_isType() {
        assertTrue(comparator.getPriority(Grant.class, database) > 0);
    }

    @Test
    public void getPriority_otherType_isNone() {
        org.junit.Assert.assertEquals(GrantComparator.PRIORITY_NONE, comparator.getPriority(liquibase.structure.core.Table.class, database));
    }

    @Test
    public void isSameObject_identicalPrivilegeGranteeAndObject_areSame() {
        Grant grant1 = new Grant(null, null, "TABLE", "orders", "SELECT", "app_user", false);
        Grant grant2 = new Grant(null, null, "TABLE", "orders", "SELECT", "app_user", true);

        assertTrue(comparator.isSameObject(grant1, grant2, database, chain));
    }

    @Test
    public void isSameObject_differentGrantee_areNotSame() {
        Grant grant1 = new Grant(null, null, "TABLE", "orders", "SELECT", "app_user", false);
        Grant grant2 = new Grant(null, null, "TABLE", "orders", "SELECT", "other_user", false);

        assertFalse(comparator.isSameObject(grant1, grant2, database, chain));
    }

    @Test
    public void isSameObject_differentPrivilege_areNotSame() {
        Grant grant1 = new Grant(null, null, "TABLE", "orders", "SELECT", "app_user", false);
        Grant grant2 = new Grant(null, null, "TABLE", "orders", "INSERT", "app_user", false);

        assertFalse(comparator.isSameObject(grant1, grant2, database, chain));
    }

    @Test
    public void hash_ignoresCaseAndGrantableFlag() {
        Grant grant1 = new Grant(null, null, "TABLE", "orders", "SELECT", "app_user", false);
        Grant grant2 = new Grant(null, null, "table", "ORDERS", "select", "APP_USER", true);

        assertArrayEquals(comparator.hash(grant1, database, chain), comparator.hash(grant2, database, chain));
    }
}
