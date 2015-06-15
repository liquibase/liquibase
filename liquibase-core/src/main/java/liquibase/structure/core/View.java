package liquibase.structure.core;

import liquibase.structure.ObjectName;

public class View extends Relation {

    private boolean containsFullDefinition;

    public View() {
    }

    public View(ObjectName name) {
        super(name);
    }

    public View(String catalogName, String schemaName, String tableName) {
        this(new ObjectName(catalogName,schemaName, tableName));
    }

    public String getDefinition() {
        return get("definition", String.class);
    }

    public void setDefinition(String definition) {
        this.set("definition", definition);
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
