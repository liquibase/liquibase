package liquibase.statement;

import liquibase.Scope;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.sql.visitor.InjectRuntimeVariablesVisitor;
import lombok.Getter;

import java.util.Optional;

import static java.util.Optional.ofNullable;

/** Statements that can return (a single) value stored into a property defined in {@code setProperty}
 *  The result is stored between the changelog's parameters the preconditions, etc to work
 *  and in the separate set of InjectRuntimeVariablesVisitor parameters as global property
 */
public abstract class ReturningSqlStatement extends AbstractSqlStatement {
    /** name of the property the result should be stored in */
    @Getter
    protected Property property;
    protected ChangeSet changeSet;
    public static final String localPrefix = "local:";

    public void setResult(String sResult) {
        DatabaseChangeLog changeLog = null;
        if(property.local) {
            changeLog = changeLog().orElseThrow();
        }
        if(InjectRuntimeVariablesVisitor.get().params().hasValue(property.name, changeLog)) {
            Scope.getCurrentScope().getLog(getClass())
                  .warning(String.format("'%s' property is already set! Cannot set new runtime value in %s!", property, location()));
        } else {
            if(property.local) {
                InjectRuntimeVariablesVisitor.get().params().setLocal(property.name, sResult, changeLog);
                changeLog.getChangeLogParameters().setLocal(property.name, sResult, changeLog);
            } else {
                InjectRuntimeVariablesVisitor.get().params().set(property.name, sResult); // Global
                changeLog().ifPresent(cl -> cl.getChangeLogParameters().set(property.name, sResult));
            }
            Scope.getCurrentScope().getLog(getClass())
                  .fine(String.format("'%s' property set runtime: '%s' in %s", property, sResult, location()));
        }
    }

    public static class Property {
        public final String name;
        public final boolean local;
        public Property(String property) {
            if((local = property.startsWith(localPrefix))) {
                this.name = property.substring(localPrefix.length());
            } else {
                this.name = property;
            }
        }
        @Override
        public String toString() {
            return (local ? "local " : "") + name;
        }
    }

    /** Set the setProperty property and the changeset */
    public ReturningSqlStatement setProperty(Property property, ChangeSet cs) {
        this.property = property;
        this.changeSet = cs;
        return this;
    }

    Optional<DatabaseChangeLog> changeLog() {
        return ofNullable( changeSet ).flatMap( cs -> ofNullable(cs.getChangeLog()));
    }

    String location() {
        return null != changeSet ? changeSet.toString() : "";
    }
}
