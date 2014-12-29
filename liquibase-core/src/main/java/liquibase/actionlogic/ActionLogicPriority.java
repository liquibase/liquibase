package liquibase.actionlogic;

public class ActionLogicPriority {

    public static final int PRIORITY_NONE = -1;
    public static final int PRIORITY_DEFAULT = 1;

    public static final ActionLogicPriority NOT_APPLICABLE = new ActionLogicPriority(PRIORITY_NONE, false);
    public static final ActionLogicPriority DEFAULT = new ActionLogicPriority(PRIORITY_DEFAULT, true);
    public static final ActionLogicPriority DEFAULT_NOT_SUPPORTED = new ActionLogicPriority(PRIORITY_DEFAULT, false);

    private int priority;
    private boolean supports;

    public ActionLogicPriority(int priority, boolean supports) {
        this.priority = priority;
        this.supports = supports;
    }

    public int getPriority() {
        return priority;
    }

    public boolean supports() {
        return supports;
    }
}
