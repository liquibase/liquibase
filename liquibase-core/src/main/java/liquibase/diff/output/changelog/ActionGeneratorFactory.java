package liquibase.diff.output.changelog;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.diff.output.DiffOutputControl;
import liquibase.servicelocator.AbstractServiceFactory;
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
        return 0;
    }

    public List<? extends Action> fixMissing(DatabaseObject missingObject, DiffOutputControl control, Scope referenceScope, Scope targetScope) {
        MissingObjectActionGenerator generator = getGenerator(MissingObjectActionGenerator.class, missingObject.getClass(), referenceScope, targetScope);
        return generator.fixMissing(missingObject, control, referenceScope, targetScope);
    }

    protected MissingObjectActionGenerator getGenerator(Class<? extends ActionGenerator> generatorType, Class<? extends DatabaseObject> objectType, Scope referenceScope, Scope targetScope) {
        return (MissingObjectActionGenerator) getService(targetScope, generatorType, objectType, referenceScope);
    }

}
