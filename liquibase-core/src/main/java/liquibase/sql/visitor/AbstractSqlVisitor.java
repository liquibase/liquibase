package liquibase.sql.visitor;

import liquibase.database.Database;

import java.util.Collection;
import java.util.Set;

public abstract class AbstractSqlVisitor implements SqlVisitor {
    private Set<String> applicableDbms;
    private boolean applyToRollback;
    private Set<String> contexts;

    public Set<String> getApplicableDbms() {
        return applicableDbms;
    }

    public void setApplicableDbms(Set<String> applicableDbms) {
        this.applicableDbms = applicableDbms;
    }

    public boolean isApplyToRollback() {
        return applyToRollback;
    }

    public void setApplyToRollback(boolean applyToRollback) {
        this.applyToRollback = applyToRollback;
    }

    public Set<String> getContexts() {
        return contexts;
    }

    public void setContexts(Set<String> contexts) {
        this.contexts = contexts;
    }
}
