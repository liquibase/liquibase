package liquibase.snapshot.transformer;

import liquibase.structure.DatabaseObject;

public abstract class CustomTransformer<T extends DatabaseObject> extends AbstractSnapshotTransformer<T> {

    public CustomTransformer() {
        super((Class<T>) DatabaseObject.class);
    }

    public CustomTransformer(Class<T>[] supportedTypes) {
        super(supportedTypes);
    }

}
