package liquibase.database;

import liquibase.Scope;
import liquibase.database.core.UnsupportedDatabaseSupplier;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;

public class ConnectionSupplierFactory {

    private final Scope scope;
    private List<ConnectionSupplier> connectionSuppliers;

    protected ConnectionSupplierFactory(Scope scope) {
        this.scope = scope;

    }

    public List<ConnectionSupplier> getConnectionSuppliers() {
        if (this.connectionSuppliers == null) {
            Iterator<ConnectionSupplier> supplierIterator = scope.getSingleton(ServiceLocator.class).findAllServices(ConnectionSupplier.class);

            if (!supplierIterator.hasNext()) {
                throw new UnexpectedLiquibaseException("Could not find ConnectionSupplier implementations");
            }

            this.connectionSuppliers = new ArrayList<>();

            while (supplierIterator.hasNext()) {
                this.connectionSuppliers.add(supplierIterator.next());
            }
        }

        return this.connectionSuppliers;
    }
}
