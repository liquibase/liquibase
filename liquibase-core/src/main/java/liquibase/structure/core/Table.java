package liquibase.structure.core;

import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Table extends Relation {


    public Table() {
        setAttribute("outgoingForeignKeys", new ArrayList<ForeignKey>());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Table that = (Table) o;

        if (this.getSchema() != null && that.getSchema() != null) {
            boolean schemasTheSame = this.getSchema().equals(that.getSchema());
            if (!schemasTheSame) {
                return false;
            }
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
