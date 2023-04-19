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

public class ChangeLogPropertyDefinedPrecondition extends AbstractPrecondition {

    private String property;
    private String value;

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public String getName() {
        return "changeLogPropertyDefined";
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    @Override
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet, ChangeExecListener changeExecListener)
            throws PreconditionFailedException, PreconditionErrorException {
        ChangeLogParameters changeLogParameters = changeLog.getChangeLogParameters();
        if (changeLogParameters == null) {
            throw new PreconditionFailedException("No Changelog properties were set", changeLog, this);
        }
        Object propertyValue = changeLogParameters.getValue(property, changeLog);
        if (propertyValue == null) {
            throw new PreconditionFailedException("Changelog property '"+ property +"' was not set", changeLog, this);
        }
        if ((value != null) && !propertyValue.toString().equals(value)) {
            throw new PreconditionFailedException("Expected changelog property '"+ property +"' to have a value of '"+value+"'.  Got '"+propertyValue+"'", changeLog, this);
        }
    }
}
