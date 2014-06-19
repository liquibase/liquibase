package liquibase.action.visitor;

import liquibase.ContextExpression;
import liquibase.action.Action;
import liquibase.change.CheckSum;
import liquibase.executor.ExecutionOptions;
import liquibase.serializer.LiquibaseSerializable;

import java.util.Set;

/**
 * ActionVisitor implementations are applied to Actions before they are executed.
 * The ActionVisitor has the option of modifying the passed Action object as it sees fit.
 *
 */
public interface ActionVisitor extends LiquibaseSerializable {

    /**
     * Returns the name of the ActionVisitor. This name is used for lookup by {@link liquibase.action.visitor.ActionVisitorFactory}
     */
    String getName();

    /**
     * Method called to modify a given Action. If this visitor should not modify the action, do nothing.
     * The visit method does not need to check the dbms or contexts, it only be called if the visitor applies to the active database and contexts
     */
    public void visit(Action action, ExecutionOptions options);

    /**
     * Returns the shortName of databases this visitor applies to.
     * Return null or empty set if it applies to all databases.
     */
    Set<String> getDbms();

    /**
     * Sets the collection of databases this visitor applies to.
     */
    void setDbms(Set<String> modifySqlDbmsList);

    /**
     * Return true if this visitor should be applied to a rollback operations.
     */
    boolean getApplyToRollback();

    /**
     * Sets if this visitor should be applied to rollback operations.
     */
    void setApplyToRollback(boolean applyOnRollback);

    /**
     * Return true if this visitor should be applied to an update operation.
     */
    boolean getApplyToUpdate();

    /**
     * Sets if this visitor should be applied to update operations.
     */
    void setApplyToUpdate(boolean applyToUpdate);


    /**
     * Return the contexts this visitor should be used in.
     * Return null or an empty ContextExpression for all contexts.
     */
    ContextExpression getContexts();

    /**
     * Sets the contexts this visitor should be used in.
     */
    void setContexts(ContextExpression contexts);

    /**
     * Generate a checksum for this visitor.
     * This should not need to take contexts or dbms into account.
     */
    CheckSum generateCheckSum();

}
