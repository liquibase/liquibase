package liquibase.database.typeconversion;

/**
 * Object representing a data type, instead of a plain string. It will be returned by
 * the getXXXType in the Database interface.
 *
 * @author dsmith
 */
public class DataType
{
    private String dataTypeName;
    private Boolean supportsPrecision;

    /**
     * Constructs a new DataType object.
     * @param dataTypeName Name of the datatype
     * @param supportsPrecision true if the dbms supports precision for the datatype
     */
    public DataType(final String dataTypeName, final Boolean supportsPrecision) {
        this.dataTypeName = dataTypeName;
        this.supportsPrecision = supportsPrecision;
    }

    /**
     * Getter for property 'dataTypeName'.
     *
     * @return Value for property 'dataTypeName'.
     */
    public String getDataTypeName()
    {
        return dataTypeName;
    }

    /**
     * Getter for property 'supportsPrecision'.
     *
     * @return Value for property 'supportsPrecision'.
     */
    public Boolean getSupportsPrecision()
    {
        return supportsPrecision;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DataType dataType = (DataType) o;

        if (!dataTypeName.equals(dataType.dataTypeName)) {
            return false;
        }
        if (!supportsPrecision.equals(dataType.supportsPrecision)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result;
        result = dataTypeName.hashCode();
        result = 31 * result + supportsPrecision.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[" + dataTypeName + ", " + supportsPrecision + "]";
    }
}
