package liquibase.structure.core;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.servicelocator.ServiceLocator;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DatabaseObjectFactory {

    private static DatabaseObjectFactory instance;
    private Set<Class<? extends DatabaseObject>> standardTypes;

    public static DatabaseObjectFactory getInstance() {
        if (instance == null) {
            instance = new DatabaseObjectFactory();
        }
        return instance;
    }

    private DatabaseObjectFactory() {
    }

    public Set<Class<? extends DatabaseObject>> parseTypes(String typesString) {
        if (StringUtils.trimToNull(typesString) == null) {
            return getStandardTypes();
        } else {
            Set<Class<? extends DatabaseObject>> returnSet = new HashSet<Class<? extends DatabaseObject>>();

            Set<String> typesToInclude = new HashSet<String>(Arrays.asList(typesString.toLowerCase().split("\\s*,\\s*")));
            Set<String> typesNotFound = new HashSet<String>(typesToInclude);

            Class<? extends DatabaseObject>[] classes = ServiceLocator.getInstance().findClasses(DatabaseObject.class);
            for (Class<? extends DatabaseObject> clazz : classes) {
                if (typesToInclude.contains(clazz.getSimpleName().toLowerCase()) || typesToInclude.contains(clazz.getSimpleName().toLowerCase()+"s")) {
                    returnSet.add(clazz);
                    typesNotFound.remove(clazz.getSimpleName().toLowerCase());
                    typesNotFound.remove(clazz.getSimpleName().toLowerCase()+"s");
                }
            }
            if (typesNotFound.size() > 0) {
                throw new UnexpectedLiquibaseException("Unknown snapshot type(s) "+StringUtils.join(typesNotFound, ", "));
            }
            return returnSet;
        }
    }

    public Set<Class<? extends DatabaseObject>> getStandardTypes() {
        if (standardTypes == null) {
            Set<Class<? extends DatabaseObject>> set = new HashSet<Class<? extends DatabaseObject>>();

            Class<? extends DatabaseObject>[] classes = ServiceLocator.getInstance().findClasses(DatabaseObject.class);
            for (Class<? extends DatabaseObject> clazz : classes) {
                try {
                    if (clazz.newInstance().snapshotByDefault()) {
                        set.add(clazz);
                    }
                } catch (Exception e) {
                    LogFactory.getLogger().info("Cannot construct "+clazz.getName()+" to determine if it should be included in the snapshot by default");
                }
            }

            standardTypes = set;
        }
        return standardTypes;
    }

}
