package liquibase.change;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.serializer.LiquibaseSerializable;

public class ConstraintsConfigTest {

    @Test
    public void constructor() throws Exception {
        ConstraintsConfig constraints = new ConstraintsConfig();
        assertNull(constraints.isDeleteCascade());
        assertNull(constraints.isInitiallyDeferred());
        assertNull(constraints.isNullable());
        assertNull(constraints.isPrimaryKey());
        assertNull(constraints.isUnique());
    }

    @Test
    public void setNullable() {
        assertEquals(true, new ConstraintsConfig().setNullable(true).isNullable());
        assertEquals(false, new ConstraintsConfig().setNullable(false).isNullable());
    }

    @Test
    public void setNullable_string() {
        assertEquals(true, new ConstraintsConfig().setNullable("true").isNullable());
        assertEquals(true, new ConstraintsConfig().setNullable("TRUE").isNullable());
        assertEquals(true, new ConstraintsConfig().setNullable("1").isNullable());

        assertEquals(false, new ConstraintsConfig().setNullable("false").isNullable());
        assertEquals(false, new ConstraintsConfig().setNullable("FALSE").isNullable());
        assertEquals(false, new ConstraintsConfig().setNullable("0").isNullable());

        assertNull(new ConstraintsConfig().setNullable("").isNullable());
        assertNull(new ConstraintsConfig().setNullable("null").isNullable());
        assertNull(new ConstraintsConfig().setNullable("NULL").isNullable());
        assertNull(new ConstraintsConfig().setNullable((String) null).isNullable());
    }


    @Test
    public void setDeleteCascade() {
        assertEquals(true, new ConstraintsConfig().setDeleteCascade(true).isDeleteCascade());
        assertEquals(false, new ConstraintsConfig().setDeleteCascade(false).isDeleteCascade());
    }

    @Test
    public void setDeleteCascade_string() {
        assertEquals(true, new ConstraintsConfig().setDeleteCascade("true").isDeleteCascade());
        assertEquals(true, new ConstraintsConfig().setDeleteCascade("TRUE").isDeleteCascade());
        assertEquals(true, new ConstraintsConfig().setDeleteCascade("1").isDeleteCascade());

        assertEquals(false, new ConstraintsConfig().setDeleteCascade("false").isDeleteCascade());
        assertEquals(false, new ConstraintsConfig().setDeleteCascade("FALSE").isDeleteCascade());
        assertEquals(false, new ConstraintsConfig().setDeleteCascade("0").isDeleteCascade());

        assertNull(new ConstraintsConfig().setDeleteCascade("").isDeleteCascade());
        assertNull(new ConstraintsConfig().setDeleteCascade("null").isDeleteCascade());
        assertNull(new ConstraintsConfig().setDeleteCascade("NULL").isDeleteCascade());
        assertNull(new ConstraintsConfig().setDeleteCascade((String) null).isDeleteCascade());
    }


    @Test
    public void setInitiallyDeferred() {
        assertEquals(true, new ConstraintsConfig().setInitiallyDeferred(true).isInitiallyDeferred());
        assertEquals(false, new ConstraintsConfig().setInitiallyDeferred(false).isInitiallyDeferred());
    }

    @Test
    public void setInitiallyDeferred_string() {
        assertEquals(true, new ConstraintsConfig().setInitiallyDeferred("true").isInitiallyDeferred());
        assertEquals(true, new ConstraintsConfig().setInitiallyDeferred("TRUE").isInitiallyDeferred());
        assertEquals(true, new ConstraintsConfig().setInitiallyDeferred("1").isInitiallyDeferred());

        assertEquals(false, new ConstraintsConfig().setInitiallyDeferred("false").isInitiallyDeferred());
        assertEquals(false, new ConstraintsConfig().setInitiallyDeferred("FALSE").isInitiallyDeferred());
        assertEquals(false, new ConstraintsConfig().setInitiallyDeferred("0").isInitiallyDeferred());

        assertNull(new ConstraintsConfig().setInitiallyDeferred("").isInitiallyDeferred());
        assertNull(new ConstraintsConfig().setInitiallyDeferred("null").isInitiallyDeferred());
        assertNull(new ConstraintsConfig().setInitiallyDeferred("NULL").isInitiallyDeferred());
        assertNull(new ConstraintsConfig().setInitiallyDeferred((String) null).isInitiallyDeferred());
    }


    @Test
    public void setPrimaryKey() {
        assertEquals(true, new ConstraintsConfig().setPrimaryKey(true).isPrimaryKey());
        assertEquals(false, new ConstraintsConfig().setPrimaryKey(false).isPrimaryKey());
    }

    @Test
    public void setPrimaryKey_string() {
        assertEquals(true, new ConstraintsConfig().setPrimaryKey("true").isPrimaryKey());
        assertEquals(true, new ConstraintsConfig().setPrimaryKey("TRUE").isPrimaryKey());
        assertEquals(true, new ConstraintsConfig().setPrimaryKey("1").isPrimaryKey());

        assertEquals(false, new ConstraintsConfig().setPrimaryKey("false").isPrimaryKey());
        assertEquals(false, new ConstraintsConfig().setPrimaryKey("FALSE").isPrimaryKey());
        assertEquals(false, new ConstraintsConfig().setPrimaryKey("0").isPrimaryKey());

        assertNull(new ConstraintsConfig().setPrimaryKey("").isPrimaryKey());
        assertNull(new ConstraintsConfig().setPrimaryKey("null").isPrimaryKey());
        assertNull(new ConstraintsConfig().setPrimaryKey("NULL").isPrimaryKey());
        assertNull(new ConstraintsConfig().setPrimaryKey((String) null).isPrimaryKey());
    }


    @Test
    public void setUnique() {
        assertEquals(true, new ConstraintsConfig().setUnique(true).isUnique());
        assertEquals(false, new ConstraintsConfig().setUnique(false).isUnique());
    }

    @Test
    public void setUnique_string() {
        assertEquals(true, new ConstraintsConfig().setUnique("true").isUnique());
        assertEquals(true, new ConstraintsConfig().setUnique("TRUE").isUnique());
        assertEquals(true, new ConstraintsConfig().setUnique("1").isUnique());

        assertEquals(false, new ConstraintsConfig().setUnique("false").isUnique());
        assertEquals(false, new ConstraintsConfig().setUnique("FALSE").isUnique());
        assertEquals(false, new ConstraintsConfig().setUnique("0").isUnique());

        assertNull(new ConstraintsConfig().setUnique("").isUnique());
        assertNull(new ConstraintsConfig().setUnique("null").isUnique());
        assertNull(new ConstraintsConfig().setUnique("NULL").isUnique());
        assertNull(new ConstraintsConfig().setUnique((String) null).isUnique());
    }


    @Test
    public void setDeferrable() {
        assertEquals(true, new ConstraintsConfig().setDeferrable(true).isDeferrable());
        assertEquals(false, new ConstraintsConfig().setDeferrable(false).isDeferrable());
    }

    @Test
    public void setDeferrable_string() {
        assertEquals(true, new ConstraintsConfig().setDeferrable("true").isDeferrable());
        assertEquals(true, new ConstraintsConfig().setDeferrable("TRUE").isDeferrable());
        assertEquals(true, new ConstraintsConfig().setDeferrable("1").isDeferrable());

        assertEquals(false, new ConstraintsConfig().setDeferrable("false").isDeferrable());
        assertEquals(false, new ConstraintsConfig().setDeferrable("FALSE").isDeferrable());
        assertEquals(false, new ConstraintsConfig().setDeferrable("0").isDeferrable());

        assertNull(new ConstraintsConfig().setDeferrable("").isDeferrable());
        assertNull(new ConstraintsConfig().setDeferrable("null").isDeferrable());
        assertNull(new ConstraintsConfig().setDeferrable("NULL").isDeferrable());
        assertNull(new ConstraintsConfig().setDeferrable((String) null).isDeferrable());
    }

    @Test(expected = UnexpectedLiquibaseException.class)
    public void setDeferrable_badString() {
        new ConstraintsConfig().setDeferrable("bad val");
    }

    @Test
    public void setPrimaryKeyName() {
        assertEquals("xyz", new ConstraintsConfig().setPrimaryKeyName("xyz").getPrimaryKeyName());
    }

    @Test
    public void setPrimaryKeyTablespace() {
        assertEquals("xyz", new ConstraintsConfig().setPrimaryKeyTablespace("xyz").getPrimaryKeyTablespace());
    }

    @Test
    public void setForeignKeyName() {
        assertEquals("xyz", new ConstraintsConfig().setForeignKeyName("xyz").getForeignKeyName());
    }

    @Test
    public void setCheck() {
        assertEquals("xyz", new ConstraintsConfig().setCheckConstraint("xyz").getCheckConstraint());
    }

    @Test
    public void setUniqueConstraintName() {
        assertEquals("xyz", new ConstraintsConfig().setUniqueConstraintName("xyz").getUniqueConstraintName());
    }

    @Test
    public void setReferences() {
        assertEquals("xyz", new ConstraintsConfig().setReferences("xyz").getReferences());
    }

    @Test
    public void getSerializedObjectName() {
        assertEquals("constraints", new ConstraintsConfig().getSerializedObjectName());
    }

    @Test
    public void getFieldsToSerialize() {
        Set<String> fields = new ConstraintsConfig().getSerializableFields();
        assertTrue(fields.contains("nullable"));
        assertTrue(fields.contains("primaryKey"));
        assertTrue(fields.contains("primaryKeyName"));
        assertTrue(fields.contains("nullable"));
    }

    @Test
    public void getSerializableFieldValue() {
        assertNull(new ConstraintsConfig().getSerializableFieldValue("nullable"));
        assertTrue((Boolean) new ConstraintsConfig().setNullable(true).getSerializableFieldValue("nullable"));

    }

    @Test
    public void getFieldSerializationType() {
        assertEquals(LiquibaseSerializable.SerializationType.NAMED_FIELD, new ConstraintsConfig().getSerializableFieldType("anythiny"));
    }
}
