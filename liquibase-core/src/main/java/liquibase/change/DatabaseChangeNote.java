package liquibase.change;

import liquibase.database.Database;
import liquibase.database.core.UnsupportedDatabase;

public @interface DatabaseChangeNote {
    public String database() default "";
    public String notes() default "";
}
