package liquibase.statement;

import liquibase.Scope;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.sql.visitor.InjectRuntimeVariablesVisitor;

import java.util.Optional;

import static java.util.Optional.ofNullable;

/** Statements that can return (a single) value stored into a property defined in {@code resultIn}
 *  The result is stored between the changelog's parameters the preconditions, etc to work
 *  and in the separate set of InjectRuntimeVariablesVisitor parameters as global property
 */
public abstract class ReturningSqlStatement extends AbstractSqlStatement {
    protected String resultIn;
    protected ChangeSet changeSet;

    /** name of the property the result should be stored */
    public String getResultIn() {
        return resultIn;
    }

    public void setResult(String sResult) {
        if(InjectRuntimeVariablesVisitor.get().params.hasValue(resultIn,null)) {
            Scope.getCurrentScope().getLog(getClass())
                  .warning(String.format("Property '%s' is already set! Cannot set new runtime value in %s!", resultIn, location()));
        } else {
            InjectRuntimeVariablesVisitor.get().params.set(resultIn, sResult); // Global
            getChangeLog().ifPresent(cl -> cl.getChangeLogParameters().set(resultIn, sResult));
            Scope.getCurrentScope().getLog(getClass())
                  .fine(String.format("Property '%s' set runtime: '%s' in %s", resultIn, sResult, location()));
        }
    }

    /** Set the resultIn property and the changeset */
    public ReturningSqlStatement setResultIn(String resultIn, ChangeSet cs) {
        this.resultIn = resultIn;
        this.changeSet = cs;
        return this;
    }

    Optional<DatabaseChangeLog> getChangeLog() {
        return ofNullable( changeSet ).flatMap( cs -> ofNullable(cs.getChangeLog()));
    }

    String location() {
        return null != changeSet ? changeSet.toString() : "";
    }
}
