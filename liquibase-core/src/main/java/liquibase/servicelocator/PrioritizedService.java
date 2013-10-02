package liquibase.servicelocator;

public interface PrioritizedService {

    final int PRIORITY_DEFAULT = 1;
    final int PRIORITY_DATABASE = 5;

    int getPriority();
}
