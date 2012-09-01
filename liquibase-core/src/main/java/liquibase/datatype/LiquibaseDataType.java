package liquibase.datatype;

import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.PrioritizedService;
import liquibase.statement.DatabaseFunction;

import java.util.ArrayList;
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

    private List<Object> parameters = new ArrayList<Object>();

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

    public Object[] getParameters() {
        return parameters.toArray();
    }
    
    public void addParameter(Object value) {
        this.parameters.add(value);
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
        return new DatabaseDataType(name, getParameters());
    }

    /**
     * Returns the value object in a format to include in SQL. Quote if necessary.
     */
    public String objectToSql(Object value, Database database) {
        if (value == null || value.toString().equalsIgnoreCase("null")) {
            return null;
        } else if (database.getDateFunctions().contains(new DatabaseFunction(value.toString()))) {
            return database.getCurrentDateTimeFunction();
        }
        return value.toString();
    }
    
    public Object sqlToObject(String value, Database database) {
        return value;
    }

    @Override
    public String toString() {
        String returnString = getName();
        if (parameters != null && parameters.size() > 0) {
            returnString += "(";
            for (Object param : parameters) {
                if (returnString == null) {
                    returnString += "NULL,";
                }
                returnString += param.toString()+",";
            }
            returnString = returnString.replaceFirst(",$", "");

//            if (getUnit() != null) {
//                returnString+=" " + getUnit();
//            }

            returnString += ")";
        }

        return returnString.trim();
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof LiquibaseDataType && toString().equals(o.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
