package liquibase.sql.visitor;

import liquibase.Contexts;
import liquibase.change.CheckSum;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.serializer.ReflectionSerializer;
import liquibase.serializer.core.string.StringChangeLogSerializer;

import java.util.Set;

public abstract class AbstractSqlVisitor implements SqlVisitor {
    private Set<String> applicableDbms;
    private boolean applyToRollback;
    private Contexts contexts;

    public Set<String> getApplicableDbms() {
        return applicableDbms;
    }

    public void setApplicableDbms(Set<String> applicableDbms) {
        this.applicableDbms = applicableDbms;
    }

    public boolean isApplyToRollback() {
        return applyToRollback;
    }

    public void setApplyToRollback(boolean applyToRollback) {
        this.applyToRollback = applyToRollback;
    }

    public Contexts getContexts() {
        return contexts;
    }

    public void setContexts(Contexts contexts) {
        this.contexts = contexts;
    }

    public CheckSum generateCheckSum() {
        return CheckSum.compute(new StringChangeLogSerializer().serialize(this, false));
    }

    public Set<String> getSerializableFields() {
        return ReflectionSerializer.getInstance().getFields(this);
    }

    public Object getSerializableFieldValue(String field) {
        return ReflectionSerializer.getInstance().getValue(this, field);
    }

    public String getSerializedObjectName() {
        return getName();
    }

    public SerializationType getSerializableFieldType(String field) {
        return SerializationType.NAMED_FIELD;
    }
}
