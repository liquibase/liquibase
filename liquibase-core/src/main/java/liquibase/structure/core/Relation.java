package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A container of columns. Usually a table or view.
 */
public abstract class Relation extends AbstractDatabaseObject {
    private Schema schema;
    protected String name;
    private String remarks;
    private List<Column> columns = new ArrayList<Column>();

    protected Relation() {
    }

    public String getName() {
        return name;
    }

    public DatabaseObject[] getContainingObjects() {
        return new DatabaseObject[]{
                getSchema()
        };

    }

    public String getRemarks() {
        return remarks;
    }

    public Relation setRemarks(String remarks) {
        this.remarks = remarks;
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
            if (column.getName().equalsIgnoreCase(columnName)) {
                return column;
            }
        }
        return null;
    }

    /**
     * @return Returns the schema.
     */
    public Schema getSchema() {
        return schema;
    }

    /**
     * @param schema The schema to set.
     */
    public Relation setSchema(Schema schema) {
        this.schema = schema;
        return this;
    }

    public int compareTo(Relation o) {
        return this.getName().compareToIgnoreCase(o.getName());
    }

}
