package liquibase.servicelocator;

import java.util.Comparator;

public interface PrioritizedService {
    Comparator<PrioritizedService> COMPARATOR = (o1, o2) -> Integer.compare(o2.getPriority(), o1.getPriority());

    int PRIORITY_DEFAULT = 1;
    int PRIORITY_DATABASE = 5;

    int getPriority();
}
