package liquibase.change;

import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ChangeParameterMetaData {
    private String parameterName;
    private String displayName;
    private String type;
    private Set<String> requiredForDatabase;
    private String mustApplyTo;

    public ChangeParameterMetaData(String parameterName, String displayName, String type, String[] requiredForDatabase, String mustApplyTo) {
        this.parameterName = parameterName;
        this.displayName = displayName;
        this.type = type;
        this.requiredForDatabase = new HashSet<String>(Arrays.asList(requiredForDatabase));
        this.mustApplyTo = mustApplyTo;
    }

    public String getParameterName() {
        return parameterName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getType() {
        return type;
    }

    public Set<String> getRequiredForDatabase() {
        return requiredForDatabase;
    }

    public boolean isRequiredFor(Database database) {
        try {
            return requiredForDatabase != null && (requiredForDatabase.contains("all") || requiredForDatabase.contains(database.getShortName()));
        } catch (NullPointerException e) {
            throw e;
        }
    }

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

    public String getMustApplyTo() {
        return mustApplyTo;
    }
}
