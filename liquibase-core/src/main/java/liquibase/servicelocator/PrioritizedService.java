package liquibase.servicelocator;

import java.util.Comparator;

public interface PrioritizedService {
    Comparator<PrioritizedService> COMPARATOR = new Comparator<PrioritizedService>() {
        @Override
        public int compare(PrioritizedService o1, PrioritizedService o2) {
            return (o1.getPriority() < o2.getPriority()) ? 1 : ((o1.getPriority() == o2.getPriority()) ? 0 : -1);
        }
    };

    int PRIORITY_DEFAULT = 1;
    int PRIORITY_DATABASE = 5;

    int getPriority();
}
