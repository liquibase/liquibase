package liquibase.changelog;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import liquibase.ContextExpression;
import liquibase.serializer.AbstractLiquibaseSerializable;

public class ChangeLogInclude extends AbstractLiquibaseSerializable implements ChangeLogChild {
    private String file;
    private Boolean relativeToChangelogFile;
    private ContextExpression context;

    @Override
    public Set<String> getSerializableFields() {
        return new LinkedHashSet<String>(Arrays.asList(
                "file",
                "relativeToChangelogFile",
                "context"));
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
}
