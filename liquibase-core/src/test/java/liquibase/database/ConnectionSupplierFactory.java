package liquibase.database;

import liquibase.Scope;
import liquibase.database.core.UnsupportedDatabaseSupplier;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class ConnectionSupplierFactory {

    private List<ConnectionSupplier> connectionSuppliers;

    protected ConnectionSupplierFactory(Scope scope) {

    }

    public List<ConnectionSupplier> getConnectionSuppliers() {
        if (this.connectionSuppliers == null) {
            Class[] supplierClasses = ServiceLocator.getInstance().findClasses(ConnectionSupplier.class);

            if (supplierClasses.length == 0) {
                throw new UnexpectedLiquibaseException("Could not find ConnectionSupplier implementations");
            }

            if (supplierClasses.length > 1) {
                List<Class> classes = new ArrayList<>(Arrays.asList(supplierClasses));
                ListIterator iterator = classes.listIterator();
                while (iterator.hasNext()) {
                    if (iterator.next().equals(UnsupportedDatabaseSupplier.class)) {
                        iterator.remove();
                    }
                }
                supplierClasses = classes.toArray(new Class[classes.size()]);
            }

            this.connectionSuppliers = new ArrayList<>();
            try {
                for (Class clazz : supplierClasses) {
                    this.connectionSuppliers.add((ConnectionSupplier) clazz.newInstance());
                }
            } catch (Exception e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }

        return this.connectionSuppliers;
    }
}
