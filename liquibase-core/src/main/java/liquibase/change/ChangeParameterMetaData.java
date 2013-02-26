package liquibase.change;

import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.util.StringUtils;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.*;

/**
 * Static metadata about a {@link Change} parameter.
 * Instances of this class are tracked within {@link ChangeMetaData} and are immutable.
 */
public class ChangeParameterMetaData {
    private String parameterName;
    private String description;
    private String exampleValue;
    private String displayName;
    private String dataType;
    private String since;
    private Set<String> requiredForDatabase;
    private String mustEqualExisting;
    private LiquibaseSerializable.SerializationType serializationType;

    public ChangeParameterMetaData(String parameterName, String displayName, String description, String exampleValue, String since, Type dataType, String[] requiredForDatabase, String mustEqualExisting, LiquibaseSerializable.SerializationType serializationType) {
        if (parameterName == null) {
            throw new UnexpectedLiquibaseException("Unexpected null parameterName");
        }
        if (parameterName.contains(" ")) {
            throw new UnexpectedLiquibaseException("Unexpected space in parameterName");
        }
        if (displayName == null) {
            throw new UnexpectedLiquibaseException("Unexpected null displayName");
        }
        if (dataType == null) {
            throw new UnexpectedLiquibaseException("Unexpected null dataType");
        }

        this.parameterName = parameterName;
        this.displayName = displayName;
        this.description = description;
        this.exampleValue = exampleValue;
        if (dataType instanceof Class) {
            this.dataType = StringUtils.lowerCaseFirst(((Class) dataType).getSimpleName());
        } else if (dataType instanceof ParameterizedType) {
            this.dataType = StringUtils.lowerCaseFirst(((Class) ((ParameterizedType) dataType).getRawType()).getSimpleName()+" of "+ StringUtils.lowerCaseFirst(((Class) ((ParameterizedType) dataType).getActualTypeArguments()[0]).getSimpleName()));
        }
        if (requiredForDatabase == null) {
            requiredForDatabase = new String[0];
        }
        this.requiredForDatabase = new HashSet<String>(Arrays.asList(requiredForDatabase));
        this.requiredForDatabase.remove("none");
        this.requiredForDatabase = Collections.unmodifiableSet(this.requiredForDatabase);

        this.mustEqualExisting = mustEqualExisting;
        this.serializationType = serializationType;
        this.since = since;
    }

    /**
     * Programmatic Name of the parameter. Will not contain spaces so it can be used for XMl tag names etc.
     * By convention, Change names should start be camel case starting with a lower case letter.
     */
    public String getParameterName() {
        return parameterName;
    }

    /**
     * A more friendly name of the parameter.
     */
    public String getDisplayName() {
        return displayName;
    }

    public String getSince() {
        return since;
    }

    /**
     * Return the data type of value stored in this parameter. Used for documentation and integration purposes as well as validation.
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Return the database types for which this parameter is required. The strings returned correspond to the values returned by {@link liquibase.database.Database#getShortName()}.
     * If the parameter is required for all datatabases, this will return the string "all" as an element.
     * If the parameter is required for no databases, this will return an empty set. Passing the string "none" to the constructor also results in an empty set.
     * This method will never return a null value
     */
    public Set<String> getRequiredForDatabase() {
        return requiredForDatabase;
    }

    /**
     * A convenience method for testing the value returned by {@link #getRequiredForDatabase()} against a given database.
     * Returns true if the {@link Database#getShortName()} method is contained in the required databases or the required database list contains the string "all"
     */
    public boolean isRequiredFor(Database database) {
        return requiredForDatabase.contains("all") || requiredForDatabase.contains(database.getShortName());
    }

    /**
     * Returns the current value of this parameter for the given Change.
     */
    public Object getCurrentValue(Change change) {
        try {
            for (PropertyDescriptor descriptor : Introspector.getBeanInfo(change.getClass()).getPropertyDescriptors()) {
                if (descriptor.getDisplayName().equals(this.parameterName)) {
                    Method readMethod = descriptor.getReadMethod();
                    if (readMethod == null) {
                        readMethod  = change.getClass().getMethod("is"+StringUtils.upperCaseFirst(descriptor.getName()));
                    }
                    return readMethod.invoke(change);
                }
            }
            throw new RuntimeException("Could not find readMethod for "+this.parameterName);
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    /**
     * Sets the value of this parameter on the given change.
     */
    public void setValue(Change change, Object value) {
        if (value instanceof String && !dataType.equals("string")) {
            try {
                if (dataType.equals("bigInteger")) {
                    value = new BigInteger((String) value);
                } else {
                    throw new UnexpectedLiquibaseException("Unknown Data Type: "+dataType);
                }
            } catch (Throwable e) {
                throw new UnexpectedLiquibaseException("Cannot convert string value '"+value+"' to "+dataType+": "+e.getMessage());
            }
        }

        try {
            for (PropertyDescriptor descriptor : Introspector.getBeanInfo(change.getClass()).getPropertyDescriptors()) {
                if (descriptor.getDisplayName().equals(this.parameterName)) {
                    Method writeMethod = descriptor.getWriteMethod();
                    if (writeMethod == null) {
                        throw new UnexpectedLiquibaseException("Could not find writeMethod for "+this.parameterName);
                    }
                    writeMethod.invoke(change, value);
                }
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException("Error setting "+this.parameterName+" to "+value, e);
        }
    }

    /**
     * Returns a dot-delimited chain of {@link liquibase.structure.DatabaseObject} fields describing what existing value this parameter would need to be set if applying the Change to a particular DatabaseObject.
     * <p></p>
     * For example, in an addColumn Change, the "name" parameter would return "column.name" because if you know of an existing Column object, the "name" parameter needs to be set to the column's name.
     * In the addColumn's "tableName" parameter, this method would return "column.table.name".
     * <p></p>
     * The values of the chain correspond to the {@link liquibase.structure.DatabaseObject#getObjectTypeName()} and {@link liquibase.structure.DatabaseObject#getAttributes()}
     * <p></p>
     * This method is used by integrations that want to generate Change instances or configurations pre-filled with data required to apply to an existing database object.
     */
    public String getMustEqualExisting() {
        return mustEqualExisting;
    }

    /**
     * Return the {@link LiquibaseSerializable.SerializationType} to use when serializing this object.
     */
    public LiquibaseSerializable.SerializationType getSerializationType() {
        return serializationType;
    }

    public Object getExampleValue() {
        if (exampleValue != null) {
            return exampleValue;
        }

        Map standardExamples = new HashMap();
        standardExamples.put("tableName", "person");
        standardExamples.put("schemaName", "public");
        standardExamples.put("catalogName", "cat");
        standardExamples.put("columnName", "id");


        if (standardExamples.containsKey(parameterName)) {
            return standardExamples.get(parameterName);
        }

        if (dataType.equals("string")) {
            return "A String";
        } else if (dataType.equals("integer")) {
            return 3;
        } else if (dataType.equals("boolean")) {
            return true;
        } else if (dataType.equals("bigInteger")) {
            return new BigInteger("371717");
        } else if (dataType.equals("list")) {
            return null; //"TODO";
        } else if (dataType.equals("sequenceNextValueFunction")) {
            return new SequenceNextValueFunction("seq_name");
        } else if (dataType.equals("databaseFunction")) {
            return new DatabaseFunction("now");
        } else {
            throw new  UnexpectedLiquibaseException("Unknown dataType "+dataType+" for "+getParameterName());
        }
    }

    public String getDescription() {
        if (description != null) {
            return description;
        }

        Map<String, String> standardDescriptions = new HashMap<String, String>();
        standardDescriptions.put("tableName", "Name of the table");
        standardDescriptions.put("schemaName", "Name of the schema");
        standardDescriptions.put("catalogName", "Name of the catalog");
        standardDescriptions.put("columnName", "Name of the column");

        return standardDescriptions.get(parameterName);

    }
}
