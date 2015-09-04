package liquibase.changelog.filter;

import java.util.ArrayList;
import java.util.List;

import liquibase.Contexts;
import liquibase.changelog.ChangeSet;
import liquibase.sql.visitor.SqlVisitor;

public class ContextChangeSetFilter implements ChangeSetFilter {
    private Contexts contexts;

    public ContextChangeSetFilter() {
        this(new Contexts());
    }

    public ContextChangeSetFilter(String... contexts) {
        this(new Contexts(contexts));
    }

    public ContextChangeSetFilter(Contexts contexts) {
        this.contexts = contexts;
    }

    @Override
    public boolean accepts(ChangeSet changeSet) {
        List<SqlVisitor> visitorsToRemove = new ArrayList<SqlVisitor>();
        for (SqlVisitor visitor : changeSet.getSqlVisitors()) {
            if (visitor.getContexts() != null && visitor.getContexts().size() > 0) {
                boolean shouldRemove = true;
                for (String context : visitor.getContexts()) {
                    if (contexts.contains(context.toLowerCase())) {
                        shouldRemove = false;
                    }
                }
                if (shouldRemove) {
                    visitorsToRemove.add(visitor);
                }
            }
        }
        changeSet.getSqlVisitors().removeAll(visitorsToRemove);

        if (contexts == null || contexts.size() == 0) {
            return true;
        }

        if (changeSet.getContexts() == null || changeSet.getContexts().size() == 0) {
            return true;
        }
        
        for (String context : changeSet.getContexts()) {
            if (contexts.contains(context.toLowerCase())) {
                return true;
            }
        }

        return false;
    }
}
