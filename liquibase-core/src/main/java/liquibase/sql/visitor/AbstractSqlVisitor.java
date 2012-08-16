package liquibase.sql.visitor;

import liquibase.change.CheckSum;
import liquibase.serializer.core.string.StringChangeLogSerializer;

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

    public CheckSum generateCheckSum() {
        return CheckSum.compute(new StringChangeLogSerializer().serialize(this));
    }

}
