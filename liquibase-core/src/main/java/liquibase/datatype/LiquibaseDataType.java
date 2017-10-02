package liquibase.datatype;

import liquibase.change.core.LoadDataChange;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.PrioritizedService;
import liquibase.statement.DatabaseFunction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Object representing a data type, instead of a plain string. It will be returned by
 * the getXXXType in the Database interface.
 */
public abstract class LiquibaseDataType implements PrioritizedService {

    private String name;
    private String[] aliases;
    private int priority;
    private int minParameters;
    private int maxParameters;

    private List<Object> parameters = new ArrayList<>();
    private String additionalInformation;
    private String rawDefinition;

    protected LiquibaseDataType(LiquibaseDataType originalType) {
    	name = originalType.name;
    	this.minParameters = originalType.minParameters;
        this.maxParameters = originalType.maxParameters;
        this.aliases = originalType.aliases;
        this.priority = originalType.priority;
    }
    
    public LiquibaseDataType() {
        DataTypeInfo dataTypeAnnotation = this.getClass().getAnnotation(DataTypeInfo.class);
        this.name = dataTypeAnnotation.name();
        this.minParameters = dataTypeAnnotation.minParameters();
        this.maxParameters = dataTypeAnnotation.maxParameters();
        this.aliases = dataTypeAnnotation.aliases();
        this.priority = dataTypeAnnotation.priority();
    }

    protected LiquibaseDataType(String name, int minParameters, int maxParameters) {
        this.name = name;
        this.minParameters = minParameters;
        this.maxParameters = maxParameters;
        this.aliases = new String[0];
        this.priority = 0;
    }

    public String getName() {
        return name;
    }

    public String[] getAliases() {
        return aliases;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public boolean supports(Database database) {
        return true;
    }
    
    public int getMinParameters(Database database) {
        return minParameters;
    }

    public int getMaxParameters(Database database) {
        return maxParameters;
    }

    /**
     * Returns an array with the parameters to the data type, e.g. NUMBER(10, 2) would return
     * an array with the items 10 and 2.
     * @return An array with the parameters. May contain 0 items.
     */
    public Object[] getParameters() {
        return parameters.toArray();
    }
    
    public void addParameter(Object value) {
        this.parameters.add(value);
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    public String getRawDefinition() {
        return rawDefinition;
    }

    public boolean validate(Database database) {
        int maxParameters = this.getMaxParameters(database);
        int minParameters = this.getMinParameters(database);

        if (parameters.size() > maxParameters) {
            throw new UnexpectedLiquibaseException("Type "+getClass()+" doesn't support "+ maxParameters+" parameters");
        }
        if (parameters.size() < minParameters) {
            throw new UnexpectedLiquibaseException("Type "+getClass()+" requires "+ minParameters+" parameters");
        }

        return true;
    }

    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof MSSQLDatabase) {
            String name = database.escapeDataTypeName(getName());
            int dataTypeMaxParameters = database.getDataTypeMaxParameters(getName());
            Object[] parameters = getParameters();
            if (dataTypeMaxParameters < parameters.length) {
                parameters = Arrays.copyOfRange(parameters, 0, dataTypeMaxParameters);
            }
            return new DatabaseDataType(name, parameters);
        }

        DatabaseDataType type = new DatabaseDataType(name.toUpperCase(), getParameters());
        type.addAdditionalInformation(additionalInformation);

        return type;
    }

    /**
     * Returns the value object in a format to include in SQL. Quote if necessary.
     */
    public String objectToSql(Object value, Database database) {
        if ((value == null) || "null".equalsIgnoreCase(value.toString())) {
            return null;
        } else if (value instanceof DatabaseFunction) {
            return functionToSql((DatabaseFunction) value, database);
        } else if (value instanceof Number) {
            return numberToSql((Number) value, database);
        }
        return otherToSql(value, database);
    }

    protected String functionToSql(DatabaseFunction function, Database database) {
        return (function == null) ? null : database.generateDatabaseFunctionValue(function);
    }

    protected String numberToSql(Number number, Database database) {
        if (number == null) {
            return null;
        }
        if (number instanceof BigDecimal) {
            return formatNumber(((BigDecimal) number).toPlainString());
        }
        return formatNumber(number.toString());
    }

    protected String otherToSql(Object value, Database database) {
        return (value == null) ? null : value.toString();
    }

    public Object sqlToObject(String value, Database database) {
        return value;
    }

    @Override
    public String toString() {
        String returnString = getName();
        if ((parameters != null) && !parameters.isEmpty() && (maxParameters > 0)) {
            returnString += "(";
            for (Object param : parameters) {
                if (returnString == null) {
                    returnString += "NULL,";
                }
                returnString += param.toString()+",";
            }
            returnString = returnString.replaceFirst(",$", "");

            returnString += ")";
        }

        if (additionalInformation != null) {
            returnString += " "+additionalInformation;
        }

        return returnString.trim();
    }

    @Override
    public boolean equals(final Object o) {
        return (o instanceof LiquibaseDataType) && toString().equals(o.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Determines if the given function name refers to the function that returns the current
     * time and date for a specific DBMS. Also returns true if the name returns the Liquibase wildcard
     * CURRENT_DATE_TIME_PLACE_HOLDER, which will later be translated into the appropriate function.
     * @param string The database function name to test
     * @param database A database object to test against
     * @return see above
     */
    protected boolean isCurrentDateTimeFunction(String string, Database database) {
        return string.toLowerCase().startsWith("current_timestamp")
                || string.toLowerCase().startsWith(DatabaseFunction.CURRENT_DATE_TIME_PLACE_HOLDER)
                || database.getCurrentDateTimeFunction().equalsIgnoreCase(string);
    }

    public void finishInitialization(String originalDefinition) {
        this.rawDefinition = originalDefinition;
    }

    protected String formatNumber(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceFirst("\\.0+$", "");
    }

    /**
     * Returns one of the four basic data types for use in LoadData: BOOLEAN, NUMERIC, DATE or STRING
     * @return one of the above Strings
     */
    public abstract LoadDataChange.LOAD_DATA_TYPE getLoadTypeName();

}
