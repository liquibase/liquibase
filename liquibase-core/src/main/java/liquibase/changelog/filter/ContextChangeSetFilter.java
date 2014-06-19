package liquibase.changelog.filter;

import liquibase.Contexts;
import liquibase.action.visitor.ActionVisitor;
import liquibase.changelog.ChangeSet;

import java.util.*;

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
        List<ActionVisitor> visitorsToRemove = new ArrayList<ActionVisitor>();
        for (ActionVisitor visitor : changeSet.getActionVisitors()) {
            if (visitor.getContexts() != null && !visitor.getContexts().matches(contexts)) {
                visitorsToRemove.add(visitor);
            }
        }
        changeSet.getActionVisitors().removeAll(visitorsToRemove);

        if (contexts == null || contexts.isEmpty()) {
            return new ChangeSetFilterResult(true, "No runtime context specified, all contexts will run", this.getClass());
        }

        if (changeSet.getContexts().isEmpty()) {
            return new ChangeSetFilterResult(true, "Change set runs under all contexts", this.getClass());
        }

        if (changeSet.getContexts().matches(contexts)) {
            return new ChangeSetFilterResult(true, "Context matches '"+contexts.toString()+"'", this.getClass());
        } else {
            return new ChangeSetFilterResult(false, "Context does not match '"+contexts.toString()+"'", this.getClass());
        }
    }
}
