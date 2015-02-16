package liquibase.snapshot;

import liquibase.Scope;
import liquibase.servicelocator.ServiceLocator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;
import liquibase.util.ObjectUtil;

import java.util.*;

public class Snapshot {
    private final SnapshotIdService snapshotIdService;
    private Map<Class<? extends DatabaseObject>, Map<? extends DatabaseObject, ? extends DatabaseObject>> objects = new HashMap<>();

    public Snapshot(Scope scope) {
        this.snapshotIdService = scope.getSingleton(SnapshotIdService.class);
    }

    public Snapshot add(DatabaseObject object) {
        Map<DatabaseObject, DatabaseObject> typeObjects = (Map<DatabaseObject, DatabaseObject>) this.objects.get(object.getClass());
        if (typeObjects == null) {
            typeObjects = new HashMap<>();
            this.objects.put(object.getClass(), typeObjects);
        }

        if (object.getSnapshotId() == null) {
            object.setSnapshotId(snapshotIdService.generateId());
        }
        typeObjects.put(object, object);

        return this;
    }

    public <T extends DatabaseObject> Set<T> get(Class<T> type) {
        return (Set<T>) Collections.unmodifiableSet(ObjectUtil.defaultIfEmpty(objects.get(type), new HashMap<T, T>()).keySet());
    }

    public <T extends DatabaseObject> T get(T type) {
        Map<T, T> typeObjects = (Map<T, T>) objects.get(type.getClass());
        if (typeObjects == null) {
            return null;
        }

        return typeObjects.get(type);
    }

    public Snapshot relate(Scope scope) {
        for (SnapshotRelateLogic logic : scope.getSingleton(ServiceLocator.class).findInstances(SnapshotRelateLogic.class)) {
            logic.relate(this);
        }

        return this;
    }
}
