package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectName;

import java.util.ArrayList;
import java.util.List;

/**
 * A container of columns. Usually a table or view.
 */
public abstract class Relation extends AbstractDatabaseObject {

    public List<Column> columns;

    protected Relation() {
    }

    protected Relation(String name) {
        super(name);
    }

    protected Relation(ObjectName name) {
        super(name);
    }

    @Override
    public DatabaseObject[] getContainingObjects() {
        return new DatabaseObject[]{
                getSchema()
        };

    }

    public String getRemarks() {
        return get("remarks", String.class);
    }

    public Relation setRemarks(String remarks) {
        set("remarks", remarks);
        return this;
    }

    public List<Column> getColumns() {
        return columns;
    }

    /**
     * Returns the column object for the given columnName.  If the column does not exist in this table,
     * return null.
     */
    public Column getColumn(String columnName) {
        for (Column column : getColumns()) {
            if (column.getName().equals(columnName)) {
                return column;
            }
        }
        return null;
    }

    /**
     * @return Returns the schema.
     */
    @Override
    public Schema getSchema() {
        return get("schema", Schema.class);
    }

    /**
     * @param schema The schema to set.
     */
    public Relation setSchema(Schema schema) {
        set("schema", schema);
        return this;
    }

    public Relation setSchema(String catalog, String schema) {
        return setSchema(new Schema(catalog, schema));
    }

    public int compareTo(Object o) {
        return this.getName().compareTo(((Relation) o).getName());
    }

}
