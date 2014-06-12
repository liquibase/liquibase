package liquibase.actiongenerator;

import liquibase.sqlgenerator.SqlGenerator;

import java.util.Comparator;

public class ActionGeneratorComparator implements Comparator<ActionGenerator> {
    @Override
    public int compare(ActionGenerator o1, ActionGenerator o2) {
        return -1 * new Integer(o1.getPriority()).compareTo(o2.getPriority());
    }
}
