package liquibase.database.typeconversion.core;

import liquibase.database.Database;

public class HibernateTypeConverter extends AbstractTypeConverter {

    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(Database database) {
        return database instanceof HibernateTypeConverter;
    }
}
