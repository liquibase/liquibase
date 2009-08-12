package liquibase.database.structure.type;

/**
 * Object representing a data type, instead of a plain string. It will be returned by
 * the getXXXType in the Database interface.
 *
 * @author dsmith
 */
public abstract class DataType
{
    public abstract String getDataTypeName();

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

        if (!getDataTypeName().equals(dataType.getDataTypeName())) {
            return false;
        }
//        if (!getSupportsPrecision().equals(dataType.getSupportsPrecision())) {
//            return false;
//        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result;
        result = getDataTypeName().hashCode();
//        result = 31 * result + getSupportsPrecision().hashCode();
        return result;
    }

//    @Override
//    public String toString() {
//        return this.getClass().getName() + "[" + getDataTypeName() + ", " + getSupportsPrecision() + "]";
//    }

    public boolean getSupportsPrecision() {
        return false;
    }
}
