package liquibase.database.typeconversion.core;

import liquibase.database.Database;

public class DefaultTypeConverter extends AbstractTypeConverter {

    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean supports(Database database) {
        return true;
    }
}
