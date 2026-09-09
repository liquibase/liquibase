package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;

import java.util.Objects;

/**
 * Represents a single GRANT of a privilege to a grantee (user or role) on a database object such as a
 * table or a schema. Unlike most {@link DatabaseObject} types, Grants are not returned as part of a standard
 * snapshot/diff ({@link #snapshotByDefault()} is {@code false}) because they are numerous, DBMS-specific, and
 * typically only of interest when explicitly auditing for permission drift, e.g. via
 * {@code liquibase diff --snapshot-types=grants} or {@code liquibase snapshot --snapshot-types=grants}.
 */
public class Grant extends AbstractDatabaseObject {

    public Grant() {
    }

    public Grant(String catalogName, String schemaName, String objectType, String objectName, String privilege, String granteeName, Boolean grantable) {
        this.setSchema(new Schema(catalogName, schemaName));
        this.setObjectType(objectType);
        this.setObjectName(objectName);
        this.setPrivilege(privilege);
        this.setGranteeName(granteeName);
        this.setGrantable(grantable);
    }

    @Override
    public DatabaseObject[] getContainingObjects() {
        return null;
    }

    /**
     * Grants don't have a natural single name, so a stable, human-readable identifier is synthesized from
     * the privilege, the securable it applies to, and the grantee, e.g. {@code SELECT ON TABLE orders TO app_user}.
     */
    @Override
    public String getName() {
        StringBuilder name = new StringBuilder();
        name.append(getPrivilege() == null ? "?" : getPrivilege());
        name.append(" ON ");
        name.append(getObjectType() == null ? "?" : getObjectType().toUpperCase());
        name.append(" ");
        name.append(getObjectName() == null ? "*" : getObjectName());
        name.append(" TO ");
        name.append(getGranteeName() == null ? "?" : getGranteeName());
        if (Boolean.TRUE.equals(getGrantable())) {
            name.append(" WITH GRANT OPTION");
        }
        return name.toString();
    }

    @Override
    public Grant setName(String name) {
        //name is derived from the other attributes, nothing to store
        return this;
    }

    @Override
    public Schema getSchema() {
        return getAttribute("schema", Schema.class);
    }

    public Grant setSchema(Schema schema) {
        setAttribute("schema", schema);
        return this;
    }

    /**
     * @return the type of object the privilege is granted on, e.g. {@code TABLE}, {@code SCHEMA}, {@code SEQUENCE}
     */
    public String getObjectType() {
        return getAttribute("objectType", String.class);
    }

    public Grant setObjectType(String objectType) {
        setAttribute("objectType", objectType);
        return this;
    }

    /**
     * @return the name of the object the privilege is granted on (e.g. a table name), or null if the privilege
     * applies to the schema/catalog as a whole
     */
    public String getObjectName() {
        return getAttribute("objectName", String.class);
    }

    public Grant setObjectName(String objectName) {
        setAttribute("objectName", objectName);
        return this;
    }

    /**
     * @return the privilege being granted, e.g. {@code SELECT}, {@code INSERT}, {@code UPDATE}, {@code DELETE}, {@code USAGE}, {@code ALL}
     */
    public String getPrivilege() {
        return getAttribute("privilege", String.class);
    }

    public Grant setPrivilege(String privilege) {
        setAttribute("privilege", privilege);
        return this;
    }

    /**
     * @return the name of the user or role the privilege was granted to
     */
    public String getGranteeName() {
        return getAttribute("granteeName", String.class);
    }

    public Grant setGranteeName(String granteeName) {
        setAttribute("granteeName", granteeName);
        return this;
    }

    /**
     * @return true if the grantee can, in turn, grant this privilege to others (e.g. "WITH GRANT OPTION")
     */
    public Boolean getGrantable() {
        return getAttribute("grantable", Boolean.class);
    }

    public Grant setGrantable(Boolean grantable) {
        setAttribute("grantable", grantable);
        return this;
    }

    @Override
    public boolean snapshotByDefault() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Grant)) {
            return false;
        }
        Grant other = (Grant) o;

        if ((getSchema() != null) && (other.getSchema() != null) && !getSchema().equals(other.getSchema())) {
            return false;
        }

        return Objects.equals(lower(getObjectType()), lower(other.getObjectType()))
                && Objects.equals(lower(getObjectName()), lower(other.getObjectName()))
                && Objects.equals(lower(getPrivilege()), lower(other.getPrivilege()))
                && Objects.equals(lower(getGranteeName()), lower(other.getGranteeName()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(lower(getObjectType()), lower(getObjectName()), lower(getPrivilege()), lower(getGranteeName()));
    }

    private static String lower(String value) {
        return value == null ? null : value.toLowerCase();
    }

    @Override
    public String toString() {
        return getName();
    }
}
