package liquibase.precondition;

import liquibase.precondition.core.PreconditionContainer;

public interface Conditional {
    public PreconditionContainer getPreconditions();

    public void setPreconditions(PreconditionContainer precond);

}
