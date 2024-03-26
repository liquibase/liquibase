package liquibase.changelog;

import liquibase.serializer.AbstractLiquibaseSerializable;
import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
public class ChangeLogProperty extends AbstractLiquibaseSerializable implements ChangeLogChild {
    private String file;
    private Boolean relativeToChangelogFile;
    private Boolean errorIfMissing;
    private String name;
    private String value;
    private String contextFilter;
    private String labels;
    private String dbms;
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

    public void setFile(String file) {
        this.file = file;
    }

    public void setRelativeToChangelogFile(Boolean getRelativeToChangelogFile) {
        this.relativeToChangelogFile = getRelativeToChangelogFile;
    }

    public void setErrorIfMissing(Boolean errorIfMissing) {
        this.errorIfMissing = errorIfMissing;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
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

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public void setDbms(String dbms) {
        this.dbms = dbms;
    }

    public void setGlobal(Boolean global) {
        this.global = global;
    }
}
