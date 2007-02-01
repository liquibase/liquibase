package liquibase.database.struture;

import java.sql.Connection;
import java.sql.SQLException;

public class Column implements DatabaseStructure {
    private Table table;
    private String name;
    private int dataType;
    private String typeName;
    private int columnSize;
    private int decimalDigits;
    private boolean nullable;
    private String remarks;
    private String columnDef;

    public Column(Table table, String name, int dataType, String typeName, int columnSize, int decimalDigits, int nullable, String remarks, String columnDef) throws SQLException {
        this.table = table;
        this.name = name;
        this.dataType = dataType;
        this.typeName = typeName;
        this.columnSize = columnSize;
        this.decimalDigits = decimalDigits;
        this.nullable = nullable == 1;
        this.remarks = remarks;
        this.columnDef = columnDef;
    }

    public Table getTable() {
        return table;
    }

    public String getName() {
        return name;
    }

    public int getDataType() {
        return dataType;
    }

    public String getTypeName() {
        return typeName;
    }

    public int getColumnSize() {
        return columnSize;
    }

    public int getDecimalDigits() {
        return decimalDigits;
    }

    public boolean isNullable() {
        return nullable;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getColumnDef() {
        return columnDef;
    }

    public String toString() {
        return getName();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Column column = (Column) o;

        if (!name.equals(column.name)) return false;
        if (!table.equals(column.table)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        if (table == null) {
            result = super.hashCode();
        } else {
            result = table.hashCode();
            result = 29 * result + name.hashCode();
        }
        return result;
    }

    public int compareTo(Object o) {
        if (o instanceof Column) {
            return toString().compareTo(o.toString());
        } else {
            return getClass().getName().compareTo(o.getClass().getName());
        }
    }

    public Connection getConnection() {
        return getTable().getConnection();
    }
}
