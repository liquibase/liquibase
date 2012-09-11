package liquibase.structure.core;

public class View extends Relation {
    private String definition;

    public View(String name) {
        super(name);
    }

    @Override
    public Relation setSchema(Schema schema) {
        return super.setSchema(schema);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        View that = (View) o;

        return name.equalsIgnoreCase(that.name);

    }

    @Override
    public int hashCode() {
        return name.toUpperCase().hashCode();
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
}
