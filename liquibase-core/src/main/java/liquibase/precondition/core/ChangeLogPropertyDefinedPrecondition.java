package liquibase.precondition.core;

import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.precondition.Precondition;
import liquibase.database.Database;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.ChangeLogParameters;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.PreconditionErrorException;

public class ChangeLogPropertyDefinedPrecondition implements Precondition {

    private String property;
    private String value;

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
    
    public Warnings warn(Database database) {
        return new Warnings();
    }

    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
        ChangeLogParameters changeLogParameters = changeLog.getChangeLogParameters();
        if (changeLogParameters == null) {
            throw new PreconditionFailedException("No Changelog properties were set", changeLog, this);
        }
        Object propertyValue = changeLogParameters.getValue(property);
        if (propertyValue == null) {
            throw new PreconditionFailedException("Changelog property '"+ property +"' was not set", changeLog, this);
        }
        if (value != null && !propertyValue.toString().equals(value)) {
            throw new PreconditionFailedException("Expected changelog property '"+ property +"' to have a value of '"+value+"'.  Got '"+propertyValue+"'", changeLog, this);
        }
    }
}
