package liquibase.changelog.filter;

import liquibase.Contexts;
import liquibase.changelog.ChangeSet;
import liquibase.util.StringUtils;
import liquibase.sql.visitor.SqlVisitor;

import java.util.*;

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
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
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
            return new ChangeSetFilterResult(true, "No runtime context specified, all contexts will run", this.getClass());
        }

        if (changeSet.getContexts() == null || changeSet.getContexts().size() == 0) {
            return new ChangeSetFilterResult(true, "Change set runs under all contexts", this.getClass());
        }
        
        for (String context : changeSet.getContexts()) {
            if (contexts.contains(context.toLowerCase())) {
                return new ChangeSetFilterResult(true, "Context matches '"+contexts.toString()+"'", this.getClass());
            }
        }

        return new ChangeSetFilterResult(false, "Context does not match '"+contexts.toString()+"'", this.getClass());
    }
}
