package liquibase.snapshot.transformer;

import liquibase.Scope;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectReference;

import java.util.HashMap;
import java.util.Map;

public class LimitTransformer<T extends DatabaseObject> extends AbstractSnapshotTransformer<T> {

    private int limit;
    private Map<Class, Map<ObjectReference, Integer>> seenByTypeThenContainer = new HashMap<>();

    @SafeVarargs
    public LimitTransformer(int limit, Class<T>... types) {
        super(types);
        this.limit = limit;
    }

    @Override
    public T transformObject(T object, Scope scope) {
        Map<ObjectReference, Integer> seenByContainer = this.seenByTypeThenContainer.get(object.getClass());
        if (seenByContainer == null) {
            seenByContainer = new HashMap<>();
            seenByTypeThenContainer.put(object.getClass(), seenByContainer);
        }

//        Integer seen = seenByContainer.get(object.getName().container);
//        if (seen == null) {
//            seen = 1;
//        } else {
//            seen = seen + 1;
//        }
//
//        if (seen > limit) {
//            return null;
//        } else {
//            seenByContainer.put(object.getName().container, seen);
//            return object;
//        }
        return null;
    }
}
