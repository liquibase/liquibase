package liquibase.precondition;

import liquibase.serializer.AbstractLiquibaseSerializable;

public abstract class AbstractPrecondition extends AbstractLiquibaseSerializable implements Precondition {

    @Override
    public String getSerializedObjectName() {
        return getName();
    }

}
