package liquibase.servicelocator;

import liquibase.plugin.Plugin;

import java.util.Comparator;

public interface PrioritizedService extends Plugin {
    /**
     * This is already "descending".
     */
    Comparator<PrioritizedService> COMPARATOR = (o1, o2) -> Integer.compare(o2.getPriority(), o1.getPriority());

    int PRIORITY_DATABASE = 5;

    /**
     *
     * This method returns a priority value for an implementation. Liquibase uses this to
     * determine which of them to use. The highest priority implementation wins.
     *
     * @return  int
     *
     */
    int getPriority();
}
