package liquibase.structure.core;

public class View extends Relation {

    private boolean containsFullDefinition;

    public View() {
    }

    public View(String name) {
        super(name);
    }

    public View(String catalogName, String schemaName, String tableName) {
        this.setSchema(new Schema(catalogName, schemaName));
        setName(tableName);
    }

    public String getDefinition() {
        return get("definition", String.class);
    }

    public void setDefinition(String definition) {
        this.set("definition", definition);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        View that = (View) o;

        return getName().equalsIgnoreCase(that.getName());

    }

    @Override
    public int hashCode() {
        return getSimpleName().toUpperCase().hashCode();
    }

    @Override
    public String toString() {
        String viewStr = getName() + " (";
        for (int i = 0; i < getColumns().size(); i++) {
            if (i > 0) {
                viewStr += "," + getColumns().get(i);
            } else {
                viewStr += getColumns().get(i);
            }
        }
        viewStr += ")";
        return viewStr;
    }

    public boolean getContainsFullDefinition() {
        return this.containsFullDefinition;
    }

    public View setContainsFullDefinition(boolean fullDefinition) {
        this.containsFullDefinition = fullDefinition;
        return this;
    }
}
