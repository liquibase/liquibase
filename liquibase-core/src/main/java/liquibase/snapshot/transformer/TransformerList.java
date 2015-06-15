package liquibase.snapshot.transformer;

import liquibase.Scope;
import liquibase.structure.DatabaseObject;
import liquibase.util.CollectionUtil;

import java.util.Arrays;
import java.util.List;

public class TransformerList<T extends DatabaseObject> extends AbstractSnapshotTransformer<T> {

    private List<SnapshotTransformer> transformers;

    public TransformerList(Class<T>[] types, SnapshotTransformer... transformers) {
        super(types);
        this.transformers = Arrays.asList(CollectionUtil.createIfNull(transformers));
    }

    public TransformerList(SnapshotTransformer... transformers) {
        this(null, transformers);
    }

    @Override
    public T transformObject(T object, Scope scope) {
        for (SnapshotTransformer transformer : transformers) {
            object = (T) transformer.transform(object, scope);
        }
        return object;
    }
}
