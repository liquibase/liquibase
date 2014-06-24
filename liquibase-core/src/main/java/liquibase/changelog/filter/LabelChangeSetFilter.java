package liquibase.changelog.filter;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.action.visitor.ActionVisitor;
import liquibase.changelog.ChangeSet;

import java.util.ArrayList;
import java.util.List;

public class LabelChangeSetFilter implements ChangeSetFilter {
    private LabelExpression labelExpression;

    public LabelChangeSetFilter() {
        this(new LabelExpression());
    }

    public LabelChangeSetFilter(LabelExpression labels) {
        this.labelExpression = labels;
    }

    @Override
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
        List<ActionVisitor> visitorsToRemove = new ArrayList<ActionVisitor>();
        for (ActionVisitor visitor : changeSet.getActionVisitors()) {
            if (visitor.getLabels() != null && !labelExpression.matches(visitor.getLabels())) {
                visitorsToRemove.add(visitor);
            }
        }
        changeSet.getActionVisitors().removeAll(visitorsToRemove);

        if (labelExpression == null || labelExpression.isEmpty()) {
            return new ChangeSetFilterResult(true, "No runtime labels specified, all labels will run", this.getClass());
        }

        if (changeSet.getLabels() == null || changeSet.getLabels().isEmpty()) {
            return new ChangeSetFilterResult(true, "Change set runs under all labels", this.getClass());
        }

        if (labelExpression.matches(changeSet.getLabels())) {
            return new ChangeSetFilterResult(true, "Labels matches '"+labelExpression.toString()+"'", this.getClass());
        } else {
            return new ChangeSetFilterResult(false, "Labels does not match '"+labelExpression.toString()+"'", this.getClass());
        }
    }
}
