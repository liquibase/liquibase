package liquibase.actionlogic;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * Factory/registry for looking up the correct ActionLogic implementation. Should normally be accessed using {@link Scope#getSingleton(Class)}, not constructed directly.
 */
public class ActionLogicFactory {

    private List<ActionLogic> logic = new ArrayList<ActionLogic>();

    /**
     * Constructor is protected because it should be used as a singleton.
     */
    protected ActionLogicFactory() {
        Class[] classes;
        try {
            classes = getActionLogicClasses();

            for (Class clazz : classes) {
                register((ActionLogic) clazz.getConstructor().newInstance());
            }

        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    /**
     * Finds all ActionLogic instances.
     */
    protected Class<? extends ActionLogic>[] getActionLogicClasses() {
        return ServiceLocator.getInstance().findClasses(ActionLogic.class);
    }

    /**
     * Registers a new ActionLogic instance for future consideration.
     */
    public void register(ActionLogic logic) {
        this.logic.add(logic);
    }

    /**
     * Returns the highest priority {@link liquibase.actionlogic.ActionLogic} implementation that supports the given action/scope pair.
     */
    public ActionLogic getActionLogic(final Action action, final Scope scope) {
        TreeSet<ActionLogic> applicable = new TreeSet<ActionLogic>(new Comparator<ActionLogic>() {
            @Override
            public int compare(ActionLogic o1, ActionLogic o2) {
                Integer o1Priority = o1.getPriority(action, scope);
                Integer o2Priority = o2.getPriority(action, scope);

                int i = o2Priority.compareTo(o1Priority);
                if (i == 0) {
                    return o1.getClass().getName().compareTo(o2.getClass().getName());
                }
                return i;
            }
        });

        for (ActionLogic logic : this.logic) {
            if (logic.getPriority(action, scope) >= 0) {
                applicable.add(logic);
            }
        }

        if (applicable.size() == 0) {
            return null;
        }
        return applicable.iterator().next();
    }
}
