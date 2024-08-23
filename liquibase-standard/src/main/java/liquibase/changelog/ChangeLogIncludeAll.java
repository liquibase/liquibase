package liquibase.changelog;

import liquibase.ContextExpression;
import liquibase.serializer.AbstractLiquibaseSerializable;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
public class ChangeLogIncludeAll extends AbstractLiquibaseSerializable implements ChangeLogChild {
    private String path;
    private Boolean errorIfMissingOrEmpty;
    private Boolean relativeToChangelogFile;
    private String resourceFilter;
    private ContextExpression context;
    private int minDepth;
    private int maxDepth;
    private String endsWithFilter;

    @Override
    public Set<String> getSerializableFields() {
        return new LinkedHashSet<>(Arrays.asList("path", "errorIfMissingOrEmpty", "relativeToChangelogFile", "resourceFilter", "context", "minDepth", "maxDepth", "endsWithFilter"));
    }

    @Override
    public String getSerializedObjectName() {
        return "includeAll";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

}
