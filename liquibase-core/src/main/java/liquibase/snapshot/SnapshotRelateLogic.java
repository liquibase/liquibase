package liquibase.snapshot;

import liquibase.Scope;
import liquibase.servicelocator.Service;

public interface SnapshotRelateLogic {

    public boolean supports(Scope scope);

    void relate(Snapshot snapshot);

}
