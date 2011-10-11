package liquibase.change;

import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ChangeParameterMetaData {
    private String parameterName;
    private String displayName;
    private String type;
    private Set<String> requiredForDatabase;

    public ChangeParameterMetaData(String parameterName, String displayName, String type, String[] requiredForDatabase) {
        this.parameterName = parameterName;
        this.displayName = displayName;
        this.type = type;
        this.requiredForDatabase = new HashSet<String>(Arrays.asList(requiredForDatabase));
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
        return requiredForDatabase.contains("all") || requiredForDatabase.contains(database.getTypeName());
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
}
