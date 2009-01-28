package liquibase.database.structure;

import liquibase.database.*;
import liquibase.util.SqlUtil;
import liquibase.log.LogFactory;

import java.sql.Types;
import java.util.Arrays;
import java.util.List;

public class Column implements DatabaseObject, Comparable<Column> {
    private Table table;
    private View view;
    private String name;
    private int dataType;
    private int columnSize;
    private int decimalDigits;
    private LengthSemantics lengthSemantics;
    private Boolean nullable;
    private String typeName;
    private Object defaultValue;
    private boolean autoIncrement = false;
    private boolean primaryKey = false;
    private boolean unique = false;

    private boolean certainDataType = true;
    private String remarks;


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

    public Boolean isNullable() {
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


    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
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
            //noinspection UnusedAssignment
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
            throw new RuntimeException(e);
        }
    }


    public boolean equals(Object o) {
        try {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Column column = (Column) o;

            return name.equalsIgnoreCase(column.name) && !(table != null ? !table.equals(column.table) : column.table != null) && !(view != null ? !view.equals(column.view) : column.view != null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public int hashCode() {
        try {
            int result;
            result = (table != null ? table.hashCode() : 0);
            result = 31 * result + (view != null ? view.hashCode() : 0);
            result = 31 * result + name.toUpperCase().hashCode();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the type name and any parameters suitable for SQL.
     */
    public String getDataTypeString(Database database) {
        List<Integer> noParens = Arrays.asList(
                Types.ARRAY,
                Types.BIGINT,
                Types.BINARY,
                Types.BIT,
                Types.BLOB,
                Types.BOOLEAN,
                Types.CLOB,
                Types.DATALINK,
                Types.DATE,
                Types.DISTINCT,
                Types.INTEGER,
                Types.JAVA_OBJECT,
                Types.LONGVARBINARY,
                Types.NULL,
                Types.OTHER,
                Types.REF,
                Types.SMALLINT,
                Types.STRUCT,
                Types.TIME,
                Types.TIMESTAMP,
                Types.TINYINT,
                Types.LONGVARCHAR);

        List<Integer> oneParam = Arrays.asList(
                Types.CHAR,
                Types.VARCHAR,
                Types.VARBINARY,
                Types.DOUBLE,
                Types.FLOAT
        );

        List<Integer> twoParams = Arrays.asList(
                Types.DECIMAL,
                Types.NUMERIC,
                Types.REAL
        );

        String translatedTypeName = this.getTypeName();
        if (database instanceof PostgresDatabase) {
            if ("bpchar".equals(translatedTypeName)) {
                translatedTypeName = "char";
            }
        }

        if (database instanceof HsqlDatabase || database instanceof DerbyDatabase) {
            if (this.getDataType() == Types.FLOAT || this.getDataType() == Types.DOUBLE) {
                return "float";
            }
        }

        String dataType;
        if (noParens.contains(this.getDataType())) {
            dataType = translatedTypeName;
        } else if (oneParam.contains(this.getDataType())) {
            if (database instanceof PostgresDatabase && translatedTypeName.equals("text")) {
                return translatedTypeName;
            } else if (database instanceof MSSQLDatabase && translatedTypeName.equals("uniqueidentifier")) {
                return translatedTypeName;
            } else if (database instanceof MySQLDatabase && (translatedTypeName.startsWith("enum(") || translatedTypeName.startsWith("set("))                   ) {
              return translatedTypeName;
            } else if (database instanceof OracleDatabase && (translatedTypeName.equals("VARCHAR2"))                   ) {
              return translatedTypeName+"("+this.getColumnSize()+" "+lengthSemantics+")";
            }
            dataType = translatedTypeName+"("+this.getColumnSize()+")";
        } else if (twoParams.contains(this.getDataType())) {
            if (database instanceof PostgresDatabase && this.getColumnSize() == 131089 ) {
                dataType = "DECIMAL";
            } else if (database instanceof MSSQLDatabase && translatedTypeName.equalsIgnoreCase("money")) {
                dataType = translatedTypeName.toUpperCase();
            } else {
                dataType = translatedTypeName+"("+this.getColumnSize()+","+this.getDecimalDigits()+")";
            }
        } else {
            LogFactory.getLogger().warning("Unknown Data Type: "+this.getDataType()+" ("+this.getTypeName()+").  Assuming it does not take parameters");
            dataType = this.getTypeName();
        }
        return dataType;
    }

    public boolean isNumeric() {
        return SqlUtil.isNumeric(getDataType());
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public boolean isDataTypeDifferent(Column otherColumn) {
        if (!this.isCertainDataType() || !otherColumn.isCertainDataType()) {
            return false;
        } else {
            return this.getDataType() != otherColumn.getDataType()
                    || this.getColumnSize() != otherColumn.getColumnSize()
                    || this.getDecimalDigits() != otherColumn.getDecimalDigits()           
                    || this.getLengthSemantics() != otherColumn.getLengthSemantics();            
        }
    }

    @SuppressWarnings({"SimplifiableIfStatement"})
    public boolean isNullabilityDifferent(Column otherColumn) {
        if (this.isNullable() == null && otherColumn.isNullable() == null) {
            return false;
        }
        if (this.isNullable() == null && otherColumn.isNullable() != null) {
            return true;
        }
        if (this.isNullable() != null && otherColumn.isNullable() == null) {
            return true;
        }
        return !this.isNullable().equals(otherColumn.isNullable());
    }

    public boolean isDifferent(Column otherColumn) {
        return isDataTypeDifferent(otherColumn) || isNullabilityDifferent(otherColumn);
    }


    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public boolean isCertainDataType() {
        return certainDataType;
    }

    public void setCertainDataType(boolean certainDataType) {
        this.certainDataType = certainDataType;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    public LengthSemantics getLengthSemantics() {
      return lengthSemantics;
    }
    
    public void setLengthSemantics(LengthSemantics lengthSemantics) {
      this.lengthSemantics = lengthSemantics;
    }

    public static enum LengthSemantics {
      CHAR, BYTE
    }
}
