package liquibase.snapshot;

import liquibase.logging.LogFactory;
import liquibase.servicelocator.ServiceLocator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import liquibase.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SnapshotControl {

    private Set<Class<? extends DatabaseObject>> types;
    private static Set<Class<? extends DatabaseObject>> defaultTypes;

    public SnapshotControl() {
        this.types = getDefaultTypes();
    }

    public SnapshotControl(Class<? extends DatabaseObject>... types) {
        if (types == null || types.length == 0) {
            this.types = getDefaultTypes();
        } else {
            this.types = new HashSet<Class<? extends DatabaseObject>>(Arrays.asList(types));
        }
    }

    public SnapshotControl(String types) {
        this.types = readTypesString(types);
    }

    private Set<Class<? extends DatabaseObject>> readTypesString(String typesString) {
        if (StringUtils.trimToNull(typesString) == null) {
            return getDefaultTypes();
        } else {
            Set<Class<? extends DatabaseObject>> returnSet = new HashSet<Class<? extends DatabaseObject>>();

            Set<String> typesToInclude = new HashSet<String>(Arrays.asList(typesString.toLowerCase().split("\\s*,\\s*")));

            Class<? extends DatabaseObject>[] classes = ServiceLocator.getInstance().findClasses(DatabaseObject.class);
            for (Class<? extends DatabaseObject> clazz : classes) {
                if (typesToInclude.contains(clazz.getSimpleName().toLowerCase()) || typesToInclude.contains(clazz.getSimpleName().toLowerCase()+"s")) {
                    returnSet.add(clazz);
                }
            }
        }
        return null;
    }

    private Set<Class<? extends DatabaseObject>> getDefaultTypes() {
        if (defaultTypes == null) {
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

            defaultTypes = set;
        }
        return defaultTypes;
    }

    public Set<Class<? extends DatabaseObject>> getTypesToInclude() {
        return types;
    }

    public boolean shouldInclude(Class<? extends DatabaseObject> type) {
        return type.equals(Catalog.class) || types.contains(type);
    }
}
