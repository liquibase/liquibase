package liquibase.database.structure.type;

import liquibase.database.Database;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.StringUtils;

/**
 * Object representing a data type, instead of a plain string. It will be returned by
 * the getXXXType in the Database interface.
 *
 * @author dsmith
 */
public abstract class DataType {

    private String dataTypeName;
    private int minParameters;
    private int maxParameters;

	// Unit of data-type precision (i.e. BYTE, CHAR for Oracle)
	private String unit;

    private String firstParameter;
    private String secondParameter;

    private String additionalInformation;

    protected DataType(String dataTypeName, int minParameters, int maxParameters) {
        this.dataTypeName = dataTypeName;
        this.minParameters = minParameters;
        this.maxParameters = maxParameters;
    }

    public String getDataTypeName() {
        return dataTypeName;
    }

    public void setDataTypeName(String dataTypeName) {
        this.dataTypeName = dataTypeName;
    }

    public int getMinParameters() {
        return minParameters;
    }

    public int getMaxParameters() {
        return maxParameters;
    }

    public String getFirstParameter() {
        return firstParameter;
    }

    public void setFirstParameter(String firstParameter) {
        if (maxParameters < 1) {
            throw new UnexpectedLiquibaseException("Type "+getClass()+" doesn't support precision but precision was specified");
        }
        this.firstParameter = firstParameter;
    }

    public String getSecondParameter() {
        return secondParameter;
    }


    public void setSecondParameter(String secondParameter) {
        if (maxParameters <2 ) {
            throw new UnexpectedLiquibaseException("Type "+getClass()+" doesn't support second parameters but one was specified");
        }

        this.secondParameter = secondParameter;
    }

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    public String convertObjectToString(Object value, Database database) {
        if (value == null) {
            return null;
        } else if (value.toString().equals("CURRENT_TIMESTAMP()")) {
            return database.getCurrentDateTimeFunction();
        } else if (value.equals(Boolean.TRUE)) {
            return TypeConverterFactory.getInstance().findTypeConverter(database).getBooleanType().getTrueBooleanValue();
        } else if (value.equals(Boolean.FALSE)) {
            return TypeConverterFactory.getInstance().findTypeConverter(database).getBooleanType().getFalseBooleanValue();
        }
        return value.toString();
    }

    @Override
    public String toString() {
        String returnString = getDataTypeName();
        if (getFirstParameter() != null) {
            returnString += "("+getFirstParameter();

            if (getSecondParameter() != null) {
                returnString+=","+getSecondParameter();
            }

	        if (getUnit() != null) {
		        returnString+=" " + getUnit();
	        }

            returnString+= ")";
        }

        returnString += " "+ StringUtils.trimToEmpty(additionalInformation);

        return returnString.trim();
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof DataType && toString().equals(o.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

//    @Override
//    public String toString() {
//        return this.getClass().getName() + "[" + getDataTypeName() + ", " + getSupportsPrecision() + "]";
//    }

    public boolean getSupportsPrecision() {
        return false;
    }
}
