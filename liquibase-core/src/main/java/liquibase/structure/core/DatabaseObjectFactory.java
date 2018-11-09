package liquibase.structure.core;

import liquibase.Scope;
import liquibase.changelog.column.LiquibaseColumn;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DatabaseObjectFactory {

    private static DatabaseObjectFactory instance;
    private Set<Class<? extends DatabaseObject>> standardTypes;

    public static synchronized DatabaseObjectFactory getInstance() {
        if (instance == null) {
            instance = new DatabaseObjectFactory();
        }
        return instance;
    }

    private DatabaseObjectFactory() {
    }

    public Set<Class<? extends DatabaseObject>> parseTypes(String typesString) {
        if (StringUtil.trimToNull(typesString) == null) {
            return getStandardTypes();
        } else {
            Set<Class<? extends DatabaseObject>> returnSet = new HashSet<>();

            Set<String> typesToInclude = new HashSet<>(Arrays.asList(typesString.toLowerCase().split("\\s*,\\s*")));
            Set<String> typesNotFound = new HashSet<>(typesToInclude);

            for (DatabaseObject object : Scope.getCurrentScope().getServiceLocator().findInstances(DatabaseObject.class)) {
                Class<? extends DatabaseObject> clazz = object.getClass();
                if (typesToInclude.contains(clazz.getSimpleName().toLowerCase())
                        || typesToInclude.contains(clazz.getSimpleName().toLowerCase()+"s")
                        || typesToInclude.contains(clazz.getSimpleName().toLowerCase()+"es") //like indexes
                        ) {
                    returnSet.add(clazz);
                    typesNotFound.remove(clazz.getSimpleName().toLowerCase());
                    typesNotFound.remove(clazz.getSimpleName().toLowerCase()+"s");
                    typesNotFound.remove(clazz.getSimpleName().toLowerCase()+"es");
                }
            }
            if (!typesNotFound.isEmpty()) {
                throw new UnexpectedLiquibaseException("Unknown snapshot type(s) "+ StringUtil.join(typesNotFound, ", "));
            }
            return returnSet;
        }
    }

    public Set<Class<? extends DatabaseObject>> getStandardTypes() {
        if (standardTypes == null) {
            Set<Class<? extends DatabaseObject>> set = new HashSet<>();

            for (DatabaseObject databaseObject : Scope.getCurrentScope().getServiceLocator().findInstances(DatabaseObject.class)) {
                if (!databaseObject.getClass().equals(LiquibaseColumn.class) && databaseObject.snapshotByDefault()) {
                    set.add(databaseObject.getClass());
                }
            }

            standardTypes = set;
        }
        return standardTypes;
    }

    public void reset() {
        this.standardTypes = null;
    }
}
