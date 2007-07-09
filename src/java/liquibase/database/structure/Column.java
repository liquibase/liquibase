package liquibase.database.structure;

public class Column implements Comparable<Column> {
    private Table table;
    private View view;
    private String name;
    private int dataType;
    private int columnSize;
    private int decimalDigits;
    private Boolean nullable;
    private String typeName;
    private String defaultValue;


    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }


    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public int getColumnSize() {
        return columnSize;
    }

    public void setColumnSize(int columnSize) {
        this.columnSize = columnSize;
    }

    public int getDecimalDigits() {
        return decimalDigits;
    }

    public void setDecimalDigits(int decimalDigits) {
        this.decimalDigits = decimalDigits;
    }

    public Boolean getNullable() {
        return nullable;
    }

    public void setNullable(Boolean nullable) {
        this.nullable = nullable;
    }


    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }


    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String toString() {
        String tableOrViewName;
        if (table == null) {
            tableOrViewName = view.getName();
        } else {
            tableOrViewName = table.getName();
        }
        return tableOrViewName +"."+getName();
    }


    public int compareTo(Column o) {
        try {
            int returnValue = 0;
            if (this.getTable() != null && o.getTable() == null) {
                return 1;
            } else if (this.getTable() == null && o.getTable() != null) {
                return -1;
            } else if (this.getTable() == null && o.getTable() == null) {
                returnValue = this.getView().compareTo(o.getView());
            } else {
                returnValue = this.getTable().compareTo(o.getTable());
            }

            if (returnValue == 0) {
                returnValue = this.getName().compareTo(o.getName());
            }

            return returnValue;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    public boolean equals(Object o) {
        try {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Column column = (Column) o;

            return name.equals(column.name) && !(table != null ? !table.equals(column.table) : column.table != null) && !(view != null ? !view.equals(column.view) : column.view != null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public int hashCode() {
        try {
            int result;
            result = (table != null ? table.hashCode() : 0);
            result = 31 * result + (view != null ? view.hashCode() : 0);
            result = 31 * result + name.hashCode();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
