package liquibase.changelog;

import liquibase.serializer.AbstractLiquibaseSerializable;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

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

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public Boolean getRelativeToChangelogFile() {
        return relativeToChangelogFile;
    }

    public void setRelativeToChangelogFile(Boolean getRelativeToChangelogFile) {
        this.relativeToChangelogFile = getRelativeToChangelogFile;
    }

    public Boolean getErrorIfMissing() {
        return errorIfMissing;
    }

    public void setErrorIfMissing(Boolean errorIfMissing) {
        this.errorIfMissing = errorIfMissing;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
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

    public String getContextFilter() {
        return contextFilter;
    }

    public ChangeLogProperty setContextFilter(String contextFilter) {
        this.contextFilter = contextFilter;
        return this;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getDbms() {
        return dbms;
    }

    public void setDbms(String dbms) {
        this.dbms = dbms;
    }

    public Boolean getGlobal() {
        return global;
    }

    public void setGlobal(Boolean global) {
        this.global = global;
    }
}
