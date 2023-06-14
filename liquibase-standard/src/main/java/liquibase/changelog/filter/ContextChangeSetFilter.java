package liquibase.changelog.filter;

import liquibase.ContextExpression;
import liquibase.Contexts;
import liquibase.changelog.ChangeSet;
import liquibase.sql.visitor.SqlVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ContextChangeSetFilter implements ChangeSetFilter {
    private Contexts contexts;

    public ContextChangeSetFilter() {
        this(new Contexts());
    }

    public ContextChangeSetFilter(Contexts contexts) {
        this.contexts = contexts;
    }

    @Override
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
        List<SqlVisitor> visitorsToRemove = new ArrayList<>();
        for (SqlVisitor visitor : changeSet.getSqlVisitors()) {
            if ((visitor.getContextFilter() != null) && !visitor.getContextFilter().matches(contexts)) {
                visitorsToRemove.add(visitor);
            }
        }
        changeSet.getSqlVisitors().removeAll(visitorsToRemove);

        if (contexts == null) {
            contexts = new Contexts();
        }

        Collection<ContextExpression> inheritableContexts = changeSet.getInheritableContextFilter();
        if (changeSet.getContextFilter().isEmpty() && inheritableContexts.isEmpty()) {
            return new ChangeSetFilterResult(true, "Changeset runs under all contexts", this.getClass(), getMdcName(), getDisplayName());
        }

        if (changeSet.getContextFilter().matches(contexts) && ContextExpression.matchesAll(inheritableContexts, contexts)) {
            return new ChangeSetFilterResult(true, "Context matches '"+contexts.toString()+"'", this.getClass(), getMdcName(), getDisplayName());
        } else {
            return new ChangeSetFilterResult(false, "Context does not match '"+contexts.toString()+"'", this.getClass(), getMdcName(), getDisplayName());
        }
    }

    @Override
    public String getMdcName() {
        return "contextMismatch";
    }

    @Override
    public String getDisplayName() {
        return "Context mismatch";
    }
}
