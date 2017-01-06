package liquibase.structure.core;

import liquibase.statement.NotNullConstraint;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Table extends Relation {


    public Table() {
        setAttribute("outgoingForeignKeys", new ArrayList<ForeignKey>());
        setAttribute("indexes", new ArrayList<Index>());
        setAttribute("uniqueConstraints", new ArrayList<UniqueConstraint>());
        setAttribute("notNullConstraints", new ArrayList<UniqueConstraint>());
    }

    public Table(String catalogName, String schemaName, String tableName) {
        this.setSchema(new Schema(catalogName, schemaName));
        setName(tableName);
    }

    public PrimaryKey getPrimaryKey() {
        return getAttribute("primaryKey", PrimaryKey.class);
    }

    public void setPrimaryKey(PrimaryKey primaryKey) {
        this.setAttribute("primaryKey", primaryKey);
    }

    public List<ForeignKey> getOutgoingForeignKeys() {
        return getAttribute("outgoingForeignKeys", List.class);
    }

    public List<Index> getIndexes() {
        return getAttribute("indexes", List.class);
    }

    public List<UniqueConstraint> getUniqueConstraints() {
        return getAttribute("uniqueConstraints", List.class);
    }
    public List<NotNullConstraint> getNotNullConstraints() {
        return getAttribute("notNullConstraints", List.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Table that = (Table) o;

        if (this.getSchema() != null && that.getSchema() != null) {
            return StringUtils.trimToEmpty(this.getSchema().getName()).equalsIgnoreCase(StringUtils.trimToEmpty(that.getSchema().getName()));
        }

        return getName().equalsIgnoreCase(that.getName());

    }

    @Override
    public int hashCode() {
        return StringUtils.trimToEmpty(getName()).toUpperCase().hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Table setName(String name) {
        return (Table) super.setName(name);
    }

}
