package liquibase.changelog;

import liquibase.serializer.AbstractLiquibaseSerializable;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
public class ChangeLogProperty extends AbstractLiquibaseSerializable implements ChangeLogChild {
    @Setter
    private String file;
    @Setter
    private Boolean relativeToChangelogFile;
    @Setter
    private Boolean errorIfMissing;
    @Setter
    private String name;
    @Setter
    private String value;
    private String contextFilter;
    @Setter
    private String labels;
    @Setter
    private String dbms;
    @Setter
    private Boolean global;

    @Override
    public Set<String> getSerializableFields() {
        return new LinkedHashSet<>(Arrays.asList("file", "relativeToChangelogFile", "errorIfMissing", "name", "value", "contextFilter", "labels", "dbms", "global"));
    }

    @Override
    public String getSerializedObjectName() {
        return "property";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    /**
     * @deprecated alias for {@link #getContextFilter()}
     */
    @Deprecated
    public String getContext() {
        return contextFilter;
    }

    /**
     * @deprecated alias for {@link #setContextFilter(String)}
     */
    @Deprecated
    public void setContext(String context) {
        this.contextFilter = context;
    }

    public ChangeLogProperty setContextFilter(String contextFilter) {
        this.contextFilter = contextFilter;
        return this;
    }

}
