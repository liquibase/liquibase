package liquibase.structure;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractDatabaseObject implements DatabaseObject {

    private Map<String, Object> attributes = new HashMap<String, Object>();

    private UUID snapshotId;

    public String getObjectTypeName() {
        return StringUtils.lowerCaseFirst(getClass().getSimpleName());
    }

    public UUID getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(UUID snapshotId) {
        if (this.snapshotId != null) {
            throw new UnexpectedLiquibaseException("snapshotId already set");
        }
        this.snapshotId = snapshotId;
    }

    public boolean snapshotByDefault() {
        return true;
    }

    public int compareTo(Object o) {
        return this.getName().compareTo(((AbstractDatabaseObject) o).getName());
    }

    public Set<String> getAttributes() {
        return attributes.keySet();
    }

    public <T> T getAttribute(String attribute, Class<T> type) {
        return (T) attributes.get(attribute);
    }

    public DatabaseObject setAttribute(String attribute, Object value) {
        if (value == null) {
            attributes.remove(attribute);
        } else {
            attributes.put(attribute, value);
        }
        return this;
    }
}
