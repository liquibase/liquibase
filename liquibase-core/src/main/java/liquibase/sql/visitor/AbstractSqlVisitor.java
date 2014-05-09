package liquibase.sql.visitor;

import liquibase.ContextExpression;
import liquibase.change.CheckSum;
import liquibase.exception.SetupException;
import liquibase.parser.core.ParsedNode;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.ReflectionSerializer;
import liquibase.serializer.core.string.StringChangeLogSerializer;
import liquibase.util.ObjectUtil;

import java.util.Set;

public abstract class AbstractSqlVisitor implements SqlVisitor {
    private Set<String> applicableDbms;
    private boolean applyToRollback;
    private ContextExpression contexts;

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
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws SetupException {
        for (ParsedNode childNode : parsedNode.getChildren()) {
            try {
                if (ObjectUtil.hasWriteProperty(this, childNode.getNodeName())) {
                    Object value = childNode.getValue();
                    if (value != null) {
                        value = value.toString();
                    }
                    ObjectUtil.setProperty(this, childNode.getNodeName(), (String) value);
                }
            } catch (Exception e) {
                throw new SetupException("Error setting property", e);
            }
        }

    }

    @Override
    public ParsedNode serialize() {
        throw new RuntimeException("TODO");
    }

}
