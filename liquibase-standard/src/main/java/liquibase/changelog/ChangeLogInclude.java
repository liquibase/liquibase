package liquibase.changelog;

import liquibase.ContextExpression;
import liquibase.serializer.AbstractLiquibaseSerializable;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class ChangeLogInclude extends AbstractLiquibaseSerializable implements ChangeLogChild {
    private String file;
    private Boolean relativeToChangelogFile;
    private Boolean errorIfMissing;
    private ContextExpression context;

    @Override
    public Set<String> getSerializableFields() {
        return new LinkedHashSet<>(Arrays.asList("file", "relativeToChangelogFile", "errorIfMissing", "context"));
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

    public Boolean getErrorIfMissing() {
        return errorIfMissing;
    }

    public void setErrorIfMissing(Boolean errorIfMissing) {
        this.errorIfMissing = errorIfMissing;
    }

    public ContextExpression getContext() {
        return context;
    }

    public void setContext(ContextExpression context) {
        this.context = context;
    }
}
