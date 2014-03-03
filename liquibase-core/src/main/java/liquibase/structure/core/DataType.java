package liquibase.structure.core;

import java.math.BigInteger;

public class DataType {

    private String typeName;

    private Integer dataTypeId;
    private Integer columnSize;
    private ColumnSizeUnit columnSizeUnit;

    private Integer decimalDigits;
    private Integer radix;
    private Integer characterOctetLength;

    public DataType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    public Integer getDataTypeId() {
        return dataTypeId;
    }

    public void setDataTypeId(Integer dataTypeId) {
        this.dataTypeId = dataTypeId;
    }

    public Integer getColumnSize() {
        return columnSize;
    }

    public void setColumnSize(Integer columnSize) {
        this.columnSize = columnSize;
    }

    public ColumnSizeUnit getColumnSizeUnit() {
        return columnSizeUnit;
    }

    public void setColumnSizeUnit(ColumnSizeUnit columnSizeUnit) {
        this.columnSizeUnit = columnSizeUnit;
    }

    public Integer getDecimalDigits() {
        return decimalDigits;
    }

    public void setDecimalDigits(Integer decimalDigits) {
        this.decimalDigits = decimalDigits;
    }

    @Override
    public String toString() {
        String value = typeName;
        boolean unsigned = false;
        if (value.toLowerCase().endsWith(" unsigned")) {
            value = value.substring(0, value.length()-" unsigned".length());
            unsigned = true;
        }

        if (columnSize == null) {
            if (decimalDigits != null) {
                value += "(*, "+decimalDigits+")";
            }
        } else {
            value += "(";
            value += columnSize;

            if (decimalDigits != null) {
                value+= ", "+decimalDigits ;
            }

            //Failing on data types such as nvarchar if included
            if (columnSizeUnit != null && columnSizeUnit.equals(ColumnSizeUnit.CHAR) && (typeName.equalsIgnoreCase("VARCHAR") || typeName.equalsIgnoreCase("VARCHAR2"))) {
                value += " " + columnSizeUnit;
            }

            value +=")";
        }

        if (unsigned) {
            value += " UNSIGNED";
        }

        return value;
    }

    public Integer getRadix() {
        return radix;
    }

    public void setRadix(Integer radix) {
        this.radix = radix;
    }

    public Integer getCharacterOctetLength() {
        return characterOctetLength;
    }

    public void setCharacterOctetLength(Integer characterOctetLength) {
        this.characterOctetLength = characterOctetLength;
    }


    public static enum ColumnSizeUnit {
        BYTE,
        CHAR,
    }
}
