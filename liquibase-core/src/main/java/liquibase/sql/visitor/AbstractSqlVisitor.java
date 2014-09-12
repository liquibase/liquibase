package liquibase.sql.visitor;

import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.change.CheckSum;
import liquibase.parser.NamespaceDetails;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.ReflectionSerializer;
import liquibase.serializer.core.string.StringChangeLogSerializer;
import liquibase.util.ObjectUtil;
import liquibase.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractSqlVisitor implements SqlVisitor {
    private Set<String> applicableDbms;
    private boolean applyToRollback;
    private ContextExpression contexts;
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

    @Override
    public ContextExpression getContexts() {
        return contexts;
    }

    @Override
    public void setContexts(ContextExpression contexts) {
        this.contexts = contexts;
    }

    public Labels getLabels() {
        return labels;
    }

    public void setLabels(Labels labels) {
        this.labels = labels;
    }

    @Override
    public CheckSum generateCheckSum() {
        return CheckSum.compute(new StringChangeLogSerializer().serialize(this, false));
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
               if (childNode.getName().equals("dbms")) {
                    this.setApplicableDbms(new HashSet<String>(StringUtils.splitAndTrim((String) childNode.getValue(), ",")));
                } else if (childNode.getName().equals("applyToRollback")) {
                   Boolean value = childNode.getValue(Boolean.class);
                   if (value != null) {
                       setApplyToRollback(value);
                   }
               } else if (childNode.getName().equals("context") || childNode.getName().equals("contexts")) {
                   setContexts(new ContextExpression((String) childNode.getValue()));
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
