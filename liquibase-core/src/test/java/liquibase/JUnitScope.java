package liquibase;

import liquibase.database.ConnectionSupplier;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.test.JUnitResourceAccessor;

import java.util.HashMap;

/**
 * Singleton root scope for JUnit tests. Use this for all Scope objects in tests to avoid re-initialization of singletons. If you want fresh Singletons for a test, use {@link JUnitEmptyScope}.
 */
public class JUnitScope extends Scope {

    private static Scope instance;

    public enum Attr {
        connectionSupplier,
        objectNameStrategy
    }

    public enum TestObjectNameStrategy {
        COMPLEX_NAMES,
        SIMPLE_NAMES
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
        return getInstance().child(Scope.Attr.database, database);
    }

    public static Scope getInstance(ConnectionSupplier supplier) {
        return getInstance()
                .child(Scope.Attr.database, supplier.getDatabase())
                .child(Attr.connectionSupplier, supplier);
    }
}
