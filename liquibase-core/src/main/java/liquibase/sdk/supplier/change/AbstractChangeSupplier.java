package liquibase.sdk.supplier.change;

import liquibase.change.Change;

public abstract class AbstractChangeSupplier<T extends Change> implements ChangeSupplier<T> {
    @Override
    public Change[] revertDatabase(T change) throws Exception {
        return null;
    }
}
