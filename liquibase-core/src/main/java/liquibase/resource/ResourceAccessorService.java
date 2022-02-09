package liquibase.resource;

import liquibase.plugin.Plugin;
import liquibase.servicelocator.PrioritizedService;

public interface ResourceAccessorService extends Plugin, PrioritizedService {
    ResourceAccessor getResourceAccessor();
}
