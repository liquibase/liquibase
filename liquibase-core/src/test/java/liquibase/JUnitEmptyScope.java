package liquibase;

import liquibase.database.ConnectionSupplier;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.test.JUnitResourceAccessor;

import java.util.HashMap;

/**
 * A scope for use in testing, but unlike {@link JUnitScope} it does not save singletons across calls.
 * This adds a performance overhead, but lets you test singletons in isolation, so normally only use this scope when you are testing singleton objects.
 */
public class JUnitEmptyScope extends Scope {

    public enum Attr {
        connectionSupplier,
    }


    private JUnitEmptyScope() throws Exception {
        super(new JUnitResourceAccessor(), new HashMap<String, Object>());
    }

    /**
     * Returns a new instance of JUnitEmptyScope.
     */
    public static Scope getNewInstance() {
        try {
            return new JUnitEmptyScope();
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public static Scope getNewInstance(Database database) {
        return getNewInstance().child(Scope.Attr.database, database);
    }

    public static Scope getNewInstance(ConnectionSupplier supplier) {
        return getNewInstance()
                .child(Scope.Attr.database, supplier.getDatabase())
                .child(Attr.connectionSupplier, supplier);
    }
}
