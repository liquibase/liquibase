package liquibase.database.structure;

import liquibase.database.Database;

import java.util.ArrayList;
import java.util.List;

/**
 * A container of columns. Usually a table or view.
 */
public abstract class Relation implements DatabaseObject, Comparable<Relation> {
    private Database database;
    private Schema schema;
    protected String name;
    private String remarks;
    private List<Column> columns = new ArrayList<Column>();

    private String rawCatalogName;
    private String rawSchemaName;

    public Relation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Database getDatabase() {
        return database;
    }

    public DatabaseObject[] getContainingObjects() {
        Schema schema = getSchema();
        if (schema == null) {
            return new DatabaseObject[]{
                    getDatabase()
            };
        } else {
            return new DatabaseObject[]{
                    schema
            };
        }

    }

    public Relation setDatabase(Database database) {
        this.database = database;
        return this;
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

    public String getRawCatalogName() {
        return rawCatalogName;
    }

    public Relation setRawCatalogName(String rawCatalogName) {
        this.rawCatalogName = rawCatalogName;
        return this;
    }

    public String getRawSchemaName() {
        return rawSchemaName;
    }

    public Relation setRawSchemaName(String rawSchemaName) {
        this.rawSchemaName = rawSchemaName;
        return this;
    }

    public int compareTo(Relation o) {
        return this.getName().compareToIgnoreCase(o.getName());
    }

}
