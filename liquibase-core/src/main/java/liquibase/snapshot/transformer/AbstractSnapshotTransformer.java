package liquibase.snapshot.transformer;

import liquibase.Scope;
import liquibase.structure.DatabaseObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractSnapshotTransformer<T extends DatabaseObject> implements SnapshotTransformer {

    private Set<Class<T>> supportedTypes;

    public AbstractSnapshotTransformer(Class<T>... supportedTypes) {
        if (supportedTypes == null || supportedTypes.length == 0) {
            supportedTypes = (Class<T>[]) new Class[] {DatabaseObject.class};
        }
        this.supportedTypes = new HashSet<>(Arrays.asList(supportedTypes));
    }

    public boolean supports(Class type) {
        for (Class<T> supported : supportedTypes) {
            if (supported.isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public final DatabaseObject transform(DatabaseObject object, Scope scope) {
        if (object == null) {
            return null;
        }
        if (supports(object.getClass())) {
            return transformObject((T) object, scope);
        } else {
            return object;
        }
    }

    public abstract T transformObject(T object, Scope scope);
}
