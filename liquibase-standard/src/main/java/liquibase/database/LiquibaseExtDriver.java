package liquibase.database;

import liquibase.resource.ResourceAccessor;

public interface LiquibaseExtDriver {
    void setResourceAccessor(ResourceAccessor accessor);
}
