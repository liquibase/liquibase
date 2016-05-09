package liquibase.changelog;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import liquibase.ContextExpression;
import liquibase.serializer.AbstractLiquibaseSerializable;

public class ChangeLogIncludeAll extends AbstractLiquibaseSerializable implements ChangeLogChild {
    private String path;
    private Boolean errorIfMissingOrEmpty;
    private Boolean relativeToChangelogFile;
    private String resourceFilter;
    private ContextExpression context;

    @Override
    public Set<String> getSerializableFields() {
        return new LinkedHashSet<String>(Arrays.asList(
                "path",
                "errorIfMissingOrEmpty",
                "relativeToChangelogFile",
                "resourceFilter",
                "context"));
    }

    @Override
    public String getSerializedObjectName() {
        return "includeAll";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean getErrorIfMissingOrEmpty() {
        return errorIfMissingOrEmpty;
    }

    public void setErrorIfMissingOrEmpty(Boolean errorIfMissingOrEmpty) {
        this.errorIfMissingOrEmpty = errorIfMissingOrEmpty;
    }

    public Boolean getRelativeToChangelogFile() {
        return relativeToChangelogFile;
    }

    public void setRelativeToChangelogFile(Boolean relativeToChangelogFile) {
        this.relativeToChangelogFile = relativeToChangelogFile;
    }

    public String getResourceFilter() {
        return resourceFilter;
    }

    public void setResourceFilter(String resourceFilter) {
        this.resourceFilter = resourceFilter;
    }

    public ContextExpression getContext() {
        return context;
    }

    public void setContext(ContextExpression context) {
        this.context = context;
    }
}
