package liquibase.sql.visitor;

import liquibase.database.Database;

import java.util.Collection;

public abstract class AbstractSqlVisitor implements SqlVisitor {
    private Collection applicableDbms;
    private boolean applyToRollback;

    public Collection getApplicableDbms() {
        return applicableDbms;
    }

    public void setApplicableDbms(Collection applicableDbms) {
        this.applicableDbms = applicableDbms;
    }

    public boolean isApplicable(Database database) {
        return applicableDbms == null || applicableDbms.contains(database.getTypeName());

    }

    public boolean isApplyToRollback() {
        return applyToRollback;
    }

    public void setApplyToRollback(boolean applyToRollback) {
        this.applyToRollback = applyToRollback;
    }
}
