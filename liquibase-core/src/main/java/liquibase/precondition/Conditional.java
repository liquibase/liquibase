package liquibase.precondition;

import liquibase.precondition.core.PreconditionContainer;

public interface Conditional {
    PreconditionContainer getPreconditions();

    void setPreconditions(PreconditionContainer precond);

}
