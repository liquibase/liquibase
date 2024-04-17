package liquibase.changelog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ChangeLogIncludeAllTest {

    @Test
    void getSerializableFields() {
        assertNotNull(new ChangeLogIncludeAll().getSerializableFields());
    }

    @Test
    void getSerializedObjectName() {
        assertNotNull(new ChangeLogIncludeAll().getSerializedObjectName());
    }

    @Test
    void getSerializedObjectNamespace() {
        assertNotNull(new ChangeLogIncludeAll().getSerializedObjectNamespace());
    }
}
