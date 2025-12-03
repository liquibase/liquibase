package liquibase.precondition.core;

import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.precondition.AbstractPrecondition;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ChangeLogPropertyDefinedPrecondition extends AbstractPrecondition {
    @Setter
    private String property;
    @Setter
    private String value;

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public String getName() {
        return "changeLogPropertyDefined";
    }

    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors(this).checkRequiredField("property", property);
    }

    @Override
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet, ChangeExecListener changeExecListener)
            throws PreconditionFailedException, PreconditionErrorException {
        ChangeLogParameters changeLogParameters = changeLog.getChangeLogParameters();
        if (changeLogParameters == null) {
            throw new PreconditionFailedException("No Changelog properties were set", changeLog, this);
        }
        Object propertyValue = changeLogParameters.getValue(property, changeLog);
        if (null == propertyValue) {
            throw new PreconditionFailedException("Changelog property '"+ property +"' was not set", changeLog, this);
        }

        if ((value != null) && !propertyValue.toString().equals(value)) {
            throw new PreconditionFailedException("Expected changelog property '"+ property +"' to have a value of '"+value+"'. Got '"+propertyValue+"'", changeLog, this);
        }
    }
}
