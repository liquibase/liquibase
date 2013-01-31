package liquibase.change;

import com.sun.istack.internal.NotNull;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.StringUtils;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Static metadata about a {@link Change} parameter.
 * Instances of this class are tracked within {@link ChangeMetaData} and are immutable.
 */
public class ChangeParameterMetaData {
    private String parameterName;
    private String displayName;
    private String dataType;
    private Set<String> requiredForDatabase;
    private String mustEqualExisting;

    public ChangeParameterMetaData(String parameterName, String displayName, Class dataType, String[] requiredForDatabase, String mustEqualExisting) {
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
        this.dataType = StringUtils.lowerCaseFirst(dataType.getSimpleName());
        if (requiredForDatabase == null) {
            requiredForDatabase = new String[0];
        }
        this.requiredForDatabase = new HashSet<String>(Arrays.asList(requiredForDatabase));
        this.requiredForDatabase.remove("none");
        this.requiredForDatabase = Collections.unmodifiableSet(this.requiredForDatabase);

        this.mustEqualExisting = mustEqualExisting;
    }

    /**
     * Programmatic Name of the parameter. Will not contain spaces so it can be used for XMl tag names etc.
     * By convention, Change names should start be camel case starting with a lower case letter.
     */
    @NotNull
    public String getParameterName() {
        return parameterName;
    }

    /**
     * A more friendly name of the parameter.
     */
    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Return the data type of value stored in this parameter. Used for documentation and integration purposes as well as validation.
     */
    @NotNull
    public String getDataType() {
        return dataType;
    }

    /**
     * Return the database types for which this parameter is required. The strings returned correspond to the values returned by {@link liquibase.database.Database#getShortName()}.
     * If the parameter is required for all datatabases, this will return the string "all" as an element.
     * If the parameter is required for no databases, this will return an empty set. Passing the string "none" to the constructor also results in an empty set.
     * This method will never return a null value
     */
    @NotNull
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
                    return descriptor.getReadMethod().invoke(change);
                }
            }
            throw new RuntimeException("Could not find readMethod for "+this.parameterName);
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
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
}
