package liquibase.snapshot;

import liquibase.Scope;
import liquibase.servicelocator.ServiceLocator;
import liquibase.snapshot.transformer.SnapshotTransformer;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectName;
import liquibase.util.CollectionUtil;
import liquibase.util.ObjectUtil;

import java.util.*;

public class Snapshot {
    private final SnapshotIdService snapshotIdService;
    private Map<Class<? extends DatabaseObject>, Set<? extends DatabaseObject>> objects = new HashMap<>();

    public Snapshot(Scope scope) {
        this.snapshotIdService = scope.getSingleton(SnapshotIdService.class);
    }

    public Snapshot add(DatabaseObject object) {
        Set<DatabaseObject> typeObjects = (Set<DatabaseObject>) this.objects.get(object.getClass());
        if (typeObjects == null) {
            typeObjects = new HashSet<>();
            this.objects.put(object.getClass(), typeObjects);
        }

        if (object.getSnapshotId() == null) {
            object.setSnapshotId(snapshotIdService.generateId());
        }
        typeObjects.add(object);

        return this;
    }

    public <T extends DatabaseObject> T get(Class<T> type, ObjectName name) {
        Set<T> objects = get(type);
        for (T obj : objects) {
            if (obj.getName().equals(name)) {
                return (T) obj;
            }
        }
        return null;
    }

    public <T extends DatabaseObject> Set<T> get(Class<T> type) {
        return (Set<T>) Collections.unmodifiableSet(CollectionUtil.createIfNull(objects.get(type)));
    }

    public <T extends DatabaseObject> T get(T example) {
        Set<T> typeObjects = (Set<T>) objects.get(example.getClass());
        if (typeObjects == null) {
            return null;
        }

        for (DatabaseObject obj : typeObjects) {
            if (obj.equals(example)) {
                return (T) obj;
            }
        }
        return null;
    }

    public Snapshot relate(Scope scope) {
        for (SnapshotRelateLogic logic : scope.getSingleton(ServiceLocator.class).findInstances(SnapshotRelateLogic.class)) {
            logic.relate(this);
        }

        return this;
    }

    public Snapshot transform(SnapshotTransformer transformer, Scope scope) {
        for (Set<? extends DatabaseObject> objects : this.objects.values()) {
            for (DatabaseObject databaseObject : objects) {
                transformer.transform(databaseObject, scope);
            }
        }

        return this;
    }
}
