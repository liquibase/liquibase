package liquibase.sql.visitor;

import liquibase.ChecksumVersion;
import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.Scope;
import liquibase.change.CheckSum;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.ReflectionSerializer;
import liquibase.serializer.core.string.StringChangeLogSerializer;
import liquibase.util.ObjectUtil;
import liquibase.util.StringUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractSqlVisitor implements SqlVisitor {
    private Set<String> applicableDbms;
    private boolean applyToRollback;
    private ContextExpression contextFilter;
    private Labels labels;

    @Override
    public Set<String> getApplicableDbms() {
        return applicableDbms;
    }

    @Override
    public void setApplicableDbms(Set<String> applicableDbms) {
        this.applicableDbms = applicableDbms;
    }

    @Override
    public boolean isApplyToRollback() {
        return applyToRollback;
    }

    @Override
    public void setApplyToRollback(boolean applyToRollback) {
        this.applyToRollback = applyToRollback;
    }

    /**
     * @deprecated use {@link #getContextFilter()}
     */
    @Deprecated
    public ContextExpression getContexts() {
        return contextFilter;
    }

    /**
     * @deprecated use {@link #setContextFilter(ContextExpression)}
     */
    @Deprecated
    public void setContexts(ContextExpression contexts) {
        this.contextFilter = contexts;
    }

    @Override
    public ContextExpression getContextFilter() {
        return contextFilter;
    }

    @Override
    public void setContextFilter(ContextExpression contextFilter) {
        this.contextFilter = contextFilter;
    }

    @Override
    public Labels getLabels() {
        return labels;
    }

    @Override
    public void setLabels(Labels labels) {
        this.labels = labels;
    }

    @Override
    public CheckSum generateCheckSum() {
        ChecksumVersion version = Scope.getCurrentScope().getChecksumVersion();
        return CheckSum.compute(new StringChangeLogSerializer(new StringChangeLogSerializer.FieldFilter(){
            @Override
            public boolean include(Object obj, String field, Object value) {
                if(Arrays.stream(getExcludedFieldFilters(version)).anyMatch(filter -> filter.equals(field))) {
                    return false;
                }
                return super.include(obj, field, value);
            }
        }).serialize(this, false));
    }

    public String[] getExcludedFieldFilters(ChecksumVersion version) {
        if (version.lowerOrEqualThan(ChecksumVersion.V8)) {
            return new String[0];
        }
        return new String[]{
                "applicableDbms",
                "contextFilter",
                "labels"
        };
    }

    @Override
    public Set<String> getSerializableFields() {
        return ReflectionSerializer.getInstance().getFields(this);
    }

    @Override
    public Object getSerializableFieldValue(String field) {
        return ReflectionSerializer.getInstance().getValue(this, field);
    }

    @Override
    public String getSerializedObjectName() {
        return getName();
    }

    @Override
    public SerializationType getSerializableFieldType(String field) {
        return SerializationType.NAMED_FIELD;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return GENERIC_CHANGELOG_EXTENSION_NAMESPACE;
    }

    @Override
    public String getSerializableFieldNamespace(String field) {
        return getSerializedObjectNamespace();
    }


    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        for (ParsedNode childNode : parsedNode.getChildren()) {
            try {
               if ("dbms".equals(childNode.getName())) {
                    this.setApplicableDbms(new HashSet<>(StringUtil.splitAndTrim((String) childNode.getValue(), ",")));
                } else if ("applyToRollback".equals(childNode.getName())) {
                   Boolean value = childNode.getValue(Boolean.class);
                   if (value != null) {
                       setApplyToRollback(value);
                   }
               } else if ("contextFilter".equals(childNode.getName()) || "context".equals(childNode.getName()) || "contexts".equals(childNode.getName())) {
                   setContextFilter(new ContextExpression((String) childNode.getValue()));
                } else  if (ObjectUtil.hasWriteProperty(this, childNode.getName())) {
                   Object value = childNode.getValue();
                   if (value != null) {
                       value = value.toString();
                   }
                   ObjectUtil.setProperty(this, childNode.getName(), (String) value);
               }
            } catch (Exception e) {
                throw new ParsedNodeException("Error setting property", e);
            }
        }

    }

    @Override
    public ParsedNode serialize() {
        throw new RuntimeException("TODO");
    }

}
