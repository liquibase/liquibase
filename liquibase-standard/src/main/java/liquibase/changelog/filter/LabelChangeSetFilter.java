    package liquibase.changelog.filter;

import liquibase.GlobalConfiguration;
import liquibase.LabelExpression;
import liquibase.Labels;
import liquibase.changelog.ChangeSet;
import liquibase.sql.visitor.SqlVisitor;
import org.apache.commons.lang3.StringUtils;

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
        List<SqlVisitor> visitorsToRemove = new ArrayList<>();
        for (SqlVisitor visitor : changeSet.getSqlVisitors()) {
            if ((visitor.getLabels() != null) && !labelExpression.matches(visitor.getLabels()))  {
                visitorsToRemove.add(visitor);
            }
        }
        changeSet.getSqlVisitors().removeAll(visitorsToRemove);

        if (labelExpression == null) {
            labelExpression = new LabelExpression();
        }

        String allLabels = changeSet.buildFullLabels();

        boolean strictValue = GlobalConfiguration.STRICT.getCurrentValue();
        if(strictValue && StringUtils.isBlank(allLabels)) {
            return new ChangeSetFilterResult(false, "labels value cannot be empty while on Strict mode", this.getClass(), "labelsEmptyOnStrictMode", "labels");
        }

        if (labelExpression.matches(new Labels(allLabels))) {
            return new ChangeSetFilterResult(true, "Labels matches '" + labelExpression.toString() + "'", this.getClass(), getMdcName(), getDisplayName());
        }
        else {
            return new ChangeSetFilterResult(false, "Labels does not match '" + labelExpression.toString() + "'", this.getClass(), getMdcName(), getDisplayName());
        }
    }

    @Override
    public String getMdcName() {
        return "labelsMismatch";
    }

    @Override
    public String getDisplayName() {
        return "Label mismatch";
    }
}
