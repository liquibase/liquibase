package liquibase.diff.output.changelog;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.structure.DatabaseObject;

import java.util.List;

public interface MissingObjectActionGenerator extends ActionGenerator {

    public List<? extends Action> fixMissing(DatabaseObject missingObject, DiffOutputControl control, Scope referenceScope, Scope targetScope);
}
