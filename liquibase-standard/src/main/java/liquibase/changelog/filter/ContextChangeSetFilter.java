package liquibase.changelog.filter;

import liquibase.ContextExpression;
import liquibase.Contexts;
import liquibase.changelog.ChangeSet;
import liquibase.sql.visitor.SqlVisitor;
import lombok.Getter;

import java.util.*;

public class ContextChangeSetFilter implements ChangeSetFilter {
    private Contexts contexts;
    @Getter
    private final Set<String> unMatchedContexts = new LinkedHashSet<>();

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
        ContextExpression providedContext = new ContextExpression(contexts.getContexts());
        // Because contexts can have logic in both the command arguments (eg --context-filter="x OR y"
        // and in the changeset, we need to evaluate matches from both sides
        // as match only checks one side of the context at a time
        if ((providedContext.matches(new Contexts(changeSet.buildFullContext()))
                || changeSet.getContextFilter().matches(contexts))
                && ContextExpression.matchesAll(inheritableContexts, contexts)) {
            unMatchedContexts.addAll(providedContext.getUnMatchedLabels());
            unMatchedContexts.addAll(changeSet.getContextFilter().getUnMatchedLabels());
            //unMatchedContexts.addAll(ContextExpression)
            return new ChangeSetFilterResult(true, "Context matches '"+contexts.toString()+"'", this.getClass(), getMdcName(), getDisplayName());
        } else {
            unMatchedContexts.addAll(providedContext.getUnMatchedLabels());
            unMatchedContexts.addAll(changeSet.getContextFilter().getUnMatchedLabels());
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
