package liquibase.diff.output.changelog;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.diff.output.DiffOutputControl;
import liquibase.servicelocator.AbstractServiceFactory;
import liquibase.snapshot.Snapshot;
import liquibase.structure.DatabaseObject;

import java.util.*;

public class ActionGeneratorFactory extends AbstractServiceFactory<ActionGenerator> {

    /**
     * Protected because should be a singleton
     */
    protected ActionGeneratorFactory(Scope scope) {
        super(scope);
    }

    @Override
    protected Class<ActionGenerator> getServiceClass() {
        return ActionGenerator.class;
    }

    @Override
    protected int getPriority(ActionGenerator obj, Scope scope, Object... args) {
        return obj.getPriority((Class<? extends DatabaseObject>) args[1], (Snapshot) args[2], (Snapshot) args[2], scope);
    }

    public List<? extends Action> fixMissing(DatabaseObject missingObject, DiffOutputControl control, Snapshot referenceSnapshot, Snapshot targetSnapshot, Scope scope) {
        MissingObjectActionGenerator generator = getGenerator(MissingObjectActionGenerator.class, missingObject.getClass(), referenceSnapshot, targetSnapshot, scope);
        return generator.fixMissing(missingObject, control, referenceSnapshot, targetSnapshot, scope);
    }

    protected MissingObjectActionGenerator getGenerator(Class<? extends ActionGenerator> generatorType, Class<? extends DatabaseObject> objectType, Snapshot referenceSnapshot, Snapshot targetSnapshot, Scope scope) {
        return (MissingObjectActionGenerator) getService(scope, generatorType, objectType, referenceSnapshot);
    }

}
