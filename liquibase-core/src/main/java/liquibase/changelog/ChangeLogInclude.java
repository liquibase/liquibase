package liquibase.changelog;

import liquibase.ContextExpression;
import liquibase.LabelExpression;
import liquibase.serializer.AbstractLiquibaseSerializable;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class ChangeLogInclude extends AbstractLiquibaseSerializable implements ChangeLogChild {
    private String file;
    private Boolean relativeToChangelogFile;
    private ContextExpression context;
    private LabelExpression labels;

    @Override
    public Set<String> getSerializableFields() {
        return new LinkedHashSet<>(Arrays.asList("file", "relativeToChangelogFile", "context", "labels"));
    }

    @Override
    public String getSerializedObjectName() {
        return "include";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public Boolean getRelativeToChangelogFile() {
        return relativeToChangelogFile;
    }

    public void setRelativeToChangelogFile(Boolean relativeToChangelogFile) {
        this.relativeToChangelogFile = relativeToChangelogFile;
    }

    public ContextExpression getContext() {
        return context;
    }

    public void setContext(ContextExpression context) {
        this.context = context;
    }

    public LabelExpression getLabels() {
        return labels;
    }

    public void setLabels(LabelExpression labels) {
        this.labels = labels;
    }
}
