package liquibase.statementlogic;

import java.util.Comparator;

public class StatementLogicComparator implements Comparator<StatementLogic> {
    @Override
    public int compare(StatementLogic o1, StatementLogic o2) {
        return -1 * new Integer(o1.getPriority()).compareTo(o2.getPriority());
    }
}
