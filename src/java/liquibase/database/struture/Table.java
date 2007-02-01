package liquibase.database.struture;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class Table implements DatabaseStructure {
    private String name;
    private String catalog;
    private String schema;
    private String type;
    private String remarks;

    private List<Column> columns;
    private List<ForeignKey> exportedKeys;
    private List<ForeignKey> importedKeys;
    private DatabaseSystem databaseSystem;

    public Table(String name, String catalog, String schema, String type, String remarks, DatabaseSystem databaseSystem) {
        this.name = name;
        this.catalog = catalog;
        this.schema = schema;
        this.type = type;
        this.remarks = remarks;

        this.columns = new ArrayList<Column>();
        this.exportedKeys = new ArrayList<ForeignKey>();
        this.importedKeys = new ArrayList<ForeignKey>();

        this.databaseSystem = databaseSystem;
    }

    public String getName() {
        return name;
    }

    public String getCatalog() {
        return catalog;
    }

    public String getSchema() {
        return schema;
    }

    public String getType() {
        return type;
    }

    public String getRemarks() {
        return remarks;
    }

    public String toString() {
        return getName();
    }

    public List<Column> getColumns() {
        return columns;
    }

    public List<ForeignKey> getExportedKeys() {
        return exportedKeys;
    }

    public List<ForeignKey> getImportedKeys() {
        return importedKeys;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Table table = (Table) o;

        if (catalog != null ? !catalog.equals(table.catalog) : table.catalog != null) return false;
        if (name != null ? !name.equals(table.name) : table.name != null) return false;
        if (schema != null ? !schema.equals(table.schema) : table.schema != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (name != null ? name.hashCode() : 0);
        result = 29 * result + (catalog != null ? catalog.hashCode() : 0);
        result = 29 * result + (schema != null ? schema.hashCode() : 0);
        return result;
    }

    public int compareTo(Object o) {
        if (o instanceof Table) {
            return toString().compareTo(o.toString());
        } else {
            return getClass().getName().compareTo(o.getClass().getName());
        }
    }

    public Connection getConnection() {
        return databaseSystem.getConnection();
    }
}
