package liquibase.structure.core;

public class Table extends Relation {

    private PrimaryKey primaryKey;
    
    public Table(String name) {
        super(name);
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
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
}
