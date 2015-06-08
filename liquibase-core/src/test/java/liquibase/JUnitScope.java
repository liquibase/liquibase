package liquibase;

import liquibase.database.ConnectionSupplier;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.test.JUnitResourceAccessor;

import java.util.HashMap;

/**
 * Singleton root scope for JUnit tests. Use this for all Scope objects in tests to avoid re-initialization of singletons.
 */
public class JUnitScope extends Scope {

    private static Scope instance;

    public enum Attr {
        connectionSupplier,
    }


    private JUnitScope() throws Exception {
        super(new JUnitResourceAccessor(), new HashMap<String, Object>());
    }

    public static Scope getInstance() {
        if (instance == null) {
            try {
                instance = new JUnitScope();
            } catch (Exception e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }
        return instance;
    }

    public static Scope getInstance(Database database) {
        return instance.child(Scope.Attr.database, database);
    }

    public static Scope getInstance(ConnectionSupplier supplier) {
        return instance
                .child(Scope.Attr.database, supplier.getDatabase())
                .child(Attr.connectionSupplier, supplier);
    }
}
