package liquibase.structure.core;

import liquibase.serializer.AbstractLiquibaseSerializable;

public class DataType extends AbstractLiquibaseSerializable {

    private String typeName;

    private Integer dataTypeId;
    private Integer columnSize;
    private ColumnSizeUnit columnSizeUnit;

    private Integer decimalDigits;
    private Integer radix;
    private Integer characterOctetLength;

    public DataType() {
    }

    public DataType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
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
        String subtypeData = null;
        String value = typeName;
        if(value.contains("FOR BIT DATA")){
            value = typeName.replaceAll("\\(.*","");
            subtypeData = " FOR BIT DATA";
        }else if (value.contains("FOR SBCS DATA")){
            value = typeName.replaceAll("\\(.*","");
            subtypeData = " FOR SBCS DATA";
        }else if (value.contains("FOR MIXED DATA")){
            value = typeName.replaceAll("\\(.*","");
            subtypeData = " FOR MIXED DATA";
        }
        boolean unsigned = false;
        if (value.toLowerCase().endsWith(" unsigned")) {
            value = value.substring(0, value.length()-" unsigned".length());
            unsigned = true;
        }

        if (columnSize == null) {
            if (decimalDigits != null) {
                value += "(*, " + decimalDigits + ")";
            }
        } else if (subtypeData != null) {
            value += "(";
            value += columnSize;
            if (columnSizeUnit != null && (typeName.equalsIgnoreCase("VARCHAR")
                    || typeName.equalsIgnoreCase("VARCHAR2")
                    || typeName.equalsIgnoreCase("CHAR"))) {
                value += " " + columnSizeUnit;
            }
            value +=")";
            value +=subtypeData;
        }else{
            value += "(";
            value += columnSize;

            if (decimalDigits != null) {
                value+= ", "+decimalDigits ;
            }

            //Failing on data types such as nvarchar if included
            if ((columnSizeUnit != null) && ("VARCHAR".equalsIgnoreCase(typeName) || "VARCHAR2".equalsIgnoreCase
                (typeName) || "CHAR".equalsIgnoreCase(typeName))
            ) {
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

    @Override
    public String getSerializedObjectName() {
        return "dataType";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_SNAPSHOT_NAMESPACE;
    }

    /**
     * Specifies the unit of a column's size. Currently, the possible units are BYTE and CHAR.
     */
    public enum ColumnSizeUnit {
        BYTE,
        CHAR,
    }
}
