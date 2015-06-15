package liquibase.snapshot.transformer;

import liquibase.Scope;
import liquibase.structure.DatabaseObject;
import liquibase.util.CollectionUtil;

import java.util.Arrays;
import java.util.List;

public class RoundRobinTransformer<T extends DatabaseObject> extends AbstractSnapshotTransformer<T> {

    private List<SnapshotTransformer> transformers;
    private int nextIndex = 0;

    public RoundRobinTransformer(Class<T>[] types, SnapshotTransformer... transformers) {
        super(types);
        this.transformers = Arrays.asList(CollectionUtil.createIfNull(transformers));
    }

    public RoundRobinTransformer(SnapshotTransformer... transformers) {
        this(null, transformers);
    }

    @Override
    public T transformObject(T object, Scope scope) {
        if (transformers.size() == 0) {
            return object;
        }
        if (nextIndex >= transformers.size()) {
            nextIndex = 0;
        }
        return (T) this.transformers.get(nextIndex++).transform(object, scope);
    }
}
