package liquibase.structure.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Table extends Relation {

    private PrimaryKey primaryKey;
    private List<ForeignKey> outgoingForeignKeys = new ArrayList<ForeignKey>();
    private List<ForeignKey> incomingForeignKeys = new ArrayList<ForeignKey>();
    private List<Index> indexes = new ArrayList<Index>();

    public Table() {
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }

    public List<ForeignKey> getOutgoingForeignKeys() {
        return outgoingForeignKeys;
    }

    public List<ForeignKey> getIncomingForeignKeys() {
        return incomingForeignKeys;
    }

    public List<Index> getIndexes() {
        return indexes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Table that = (Table) o;

        return name.equalsIgnoreCase(that.name);

    }

    @Override
    public int hashCode() {
        return name.toUpperCase().hashCode();
    }

    @Override
    public String toString() {
    	return getName();
    }

    public Table setName(String name) {
        this.name = name;
        return this;
    }

}
