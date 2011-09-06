package liquibase.database.structure;

import java.math.BigInteger;

import liquibase.util.SqlUtil;

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
    private BigInteger startWith;
    private BigInteger incrementBy;
    private boolean primaryKey = false;
    private boolean unique = false;
	// indicates that data type need to initialize precision and scale
	// i.e. NUMBER vs NUMBER(22,0)
	private boolean initPrecision = true;

    private boolean certainDataType = true;
    private String remarks;

	// used for PK's index configuration
	private String tablespace;

    public Table getTable() {
        return table;
    }

    public DatabaseObject[] getContainingObjects() {
        return new DatabaseObject[] {
                getTable()
        };
    }

    public Column setTable(Table table) {
        this.table = table;

        return this;
    }


    public View getView() {
        return view;
    }

    public Column setView(View view) {
        this.view = view;

        return this;
    }

	public String getTablespace() {
		return tablespace;
	}

	public Column setTablespace(String tablespace) {
		this.tablespace = tablespace;
		return  this;
	}

	public String getName() {
        return name;
    }

    public Column setName(String name) {
        this.name = name;

        return this;
    }


    public int getDataType() {
        return dataType;
    }

    public Column setDataType(int dataType) {
        this.dataType = dataType;

        return this;
    }

    public int getColumnSize() {
        return columnSize;
    }

    public Column setColumnSize(int columnSize) {
        this.columnSize = columnSize;

        return this;
    }

    public int getDecimalDigits() {
        return decimalDigits;
    }

    public Column setDecimalDigits(int decimalDigits) {
        this.decimalDigits = decimalDigits;

        return this;
    }

    public Boolean isNullable() {
        return nullable;
    }

    public Column setNullable(Boolean nullable) {
        this.nullable = nullable;

        return this;
    }


    public String getTypeName() {
        return typeName;
    }

    public Column setTypeName(String typeName) {
        this.typeName = typeName;

        return this;
    }


    public Object getDefaultValue() {
        return defaultValue;
    }

    public Column setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;

        return this;
    }

    @Override
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


    @Override
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

    @Override
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

    public boolean isNumeric() {
        return SqlUtil.isNumeric(getDataType());
    }

    public boolean isUnique() {
        return unique;
    }

    public Column setUnique(boolean unique) {
        this.unique = unique;

        return this;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public Column setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;

        return this;
    }

    public BigInteger getStartWith() {
    	return startWith;
    }
    
    public Column setStartWith(BigInteger startWith) {
    	this.startWith = startWith;
    	
    	return this;
    }
    
    public BigInteger getIncrementBy() {
    	return incrementBy;
    }
    
    public Column setIncrementBy(BigInteger incrementBy) {
    	this.incrementBy = incrementBy;
    	
    	return this;
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

    public Column setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;

        return this;
    }

    public boolean isCertainDataType() {
        return certainDataType;
    }

    public Column setCertainDataType(boolean certainDataType) {
        this.certainDataType = certainDataType;

        return this;
    }

    public String getRemarks() {
        return remarks;
    }

    public Column setRemarks(String remarks) {
        this.remarks = remarks;

        return this;
    }

	public boolean isInitPrecision() {
		return initPrecision;
	}

	public void setInitPrecision(boolean initPrecision) {
		this.initPrecision = initPrecision;
	}

	public LengthSemantics getLengthSemantics() {
      return lengthSemantics;
    }

    public Column setLengthSemantics(LengthSemantics lengthSemantics) {
      this.lengthSemantics = lengthSemantics;

        return this;
    }

    public static enum LengthSemantics {
      CHAR, BYTE
    }
}

