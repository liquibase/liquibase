package liquibase.action;

import liquibase.AbstractExtensibleObject;
import liquibase.Scope;
import liquibase.action.Action;
import liquibase.structure.ObjectName;
import liquibase.util.StringUtils;

/**
 * Convenience standard implementation of {@link liquibase.action.Action}.
 * If your class is an update or query, be sure to also implement {@link liquibase.action.UpdateAction} or {@link liquibase.action.UpdateAction}.
 */
public abstract class AbstractAction extends AbstractExtensibleObject implements Action {

    /**
     * Standard implementation uses reflection to list out the set properties on this object.
     */
    @Override
    public String describe() {
        String name = getClass().getSimpleName();
        name = name.replaceFirst("Action$", "");
        name = StringUtils.lowerCaseFirst(name);
        return name+"("+ StringUtils.join(this, ", ", new StringUtils.DefaultFormatter())+")";
    }

    /**
     * Default implementation compares the output of the {@link #describe()} method.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Action)) {
            return false;
        }
        return this.describe().equals(((Action) obj).describe());
    }

    @Override
    public int hashCode() {
        return this.describe().hashCode();
    }

    @Override
    public String toString() {
        return this.describe();
    }
}
