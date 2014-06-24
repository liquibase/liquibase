package liquibase.action.visitor;

import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.change.CheckSum;
import liquibase.serializer.AbstractLiquibaseSerializable;
import liquibase.serializer.ReflectionSerializer;
import liquibase.serializer.core.string.StringChangeLogSerializer;

import java.util.Set;

/**
 * Convenience base class for ActionVisitors. Normally it is best to extend this class vs. implementing ActionVisitor directly.
 * If you are creating an ActionVisitor specifically for SQL, consider extending {@link liquibase.action.visitor.AbstractSqlVisitor}
 */
public abstract class AbstractActionVisitor extends AbstractLiquibaseSerializable implements ActionVisitor {

    private Set<String> dbms;
    private boolean applyToUpdate = true;
    private boolean applyToRollback = false;
    private ContextExpression contexts;
    private Labels labels;

    /**
     * Default return value is null.
     */
    @Override
    public Set<String> getDbms() {
        return dbms;
    }

    @Override
    public void setDbms(Set<String> applicableDbms) {
        this.dbms = applicableDbms;
    }

    /**
     * Default return value is false
     */
    @Override
    public boolean getApplyToRollback() {
        return applyToRollback;
    }

    @Override
    public void setApplyToRollback(boolean applyToRollback) {
        this.applyToRollback = applyToRollback;
    }

    /**
     * Default return value is true
     */
    @Override
    public boolean getApplyToUpdate() {
        return applyToUpdate;
    }

    @Override
    public void setApplyToUpdate(boolean applyToUpdate) {
        this.applyToUpdate = applyToUpdate;
    }

    /**
     * Default return value is null
     */
    @Override
    public ContextExpression getContexts() {
        return contexts;
    }

    @Override
    public void setContexts(ContextExpression contexts) {
        this.contexts = contexts;
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
        return CheckSum.compute(new StringChangeLogSerializer(new StringChangeLogSerializer.SkipFieldsFilter("contexts", "dbms", "applyToUpdate", "applyToRollback")).serialize(this, false));
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

    /**
     * Default return value is the generic changelog extension namespace
     */
    @Override
    public String getSerializedObjectNamespace() {
        return GENERIC_CHANGELOG_EXTENSION_NAMESPACE;
    }
}
