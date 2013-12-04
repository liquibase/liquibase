package liquibase.sql.visitor;

import liquibase.Contexts;
import liquibase.change.CheckSum;
import liquibase.serializer.ReflectionSerializer;
import liquibase.serializer.core.string.StringChangeLogSerializer;

import java.util.Set;

public abstract class AbstractSqlVisitor implements SqlVisitor {
    private Set<String> applicableDbms;
    private boolean applyToRollback;
    private Contexts contexts;

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
    public Contexts getContexts() {
        return contexts;
    }

    @Override
    public void setContexts(Contexts contexts) {
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
}
