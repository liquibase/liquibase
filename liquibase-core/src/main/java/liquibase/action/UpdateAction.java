package liquibase.action;

/**
 * Describes an {@link liquibase.action.Action} that updates data in a database or any other location.
 * The logic for the update goes in an implementation of {@link liquibase.actionlogic.ActionLogic}
 */
public interface UpdateAction extends Action {

}
