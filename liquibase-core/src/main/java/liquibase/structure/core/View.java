package liquibase.structure.core;

import liquibase.util.StringUtils;

public class View extends Relation {

    private boolean containsFullDefinition;

    public View() {
    }

    public View(String catalogName, String schemaName, String tableName) {
        this.setSchema(new Schema(catalogName, schemaName));
        setName(tableName);
    }

    @Override
    public Relation setSchema(Schema schema) {
        return super.setSchema(schema);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public String getDefinition() {
        return getAttribute("definition", String.class);
    }

    public void setDefinition(String definition) {
        this.setAttribute("definition", definition);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;

        View that = (View) o;

        if ((this.getSchema() != null) && (that.getSchema() != null)) {
            boolean schemasEqual = StringUtils.trimToEmpty(this.getSchema().getName()).equalsIgnoreCase(StringUtils.trimToEmpty(that.getSchema().getName()));
            if (!schemasEqual) {
                return false;
            }
        }

        return getName().equalsIgnoreCase(that.getName());

    }

    @Override
    public int hashCode() {
        return getName().toUpperCase().hashCode();
    }

    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public View setName(String name) {
        return (View) super.setName(name);
    }


    public boolean getContainsFullDefinition() {
        return this.containsFullDefinition;
    }

    public View setContainsFullDefinition(boolean fullDefinition) {
        this.containsFullDefinition = fullDefinition;
        return this;
    }
}
