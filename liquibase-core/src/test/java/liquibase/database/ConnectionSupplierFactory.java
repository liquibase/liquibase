package liquibase.database;

import liquibase.Scope;
import liquibase.database.core.UnsupportedDatabaseSupplier;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;

public class ConnectionSupplierFactory {

    private List<ConnectionSupplier> connectionSuppliers;

    protected ConnectionSupplierFactory(Scope scope) {

    }

    public List<ConnectionSupplier> getConnectionSuppliers() {
        if (this.connectionSuppliers == null) {
            Iterator<ConnectionSupplier> supplierIterator = ServiceLocator.getInstance().findAllServices(ConnectionSupplier.class);

            if (!supplierIterator.hasNext()) {
                throw new UnexpectedLiquibaseException("Could not find ConnectionSupplier implementations");
            }

            this.connectionSuppliers = new ArrayList<>();

            while (supplierIterator.hasNext()) {
                ConnectionSupplier supplier = supplierIterator.next();
                if (supplier.getClass().equals(UnsupportedDatabaseSupplier.class)) {
                    continue;
                }
                this.connectionSuppliers.add(supplier);
            }
        }

        return this.connectionSuppliers;
    }
}
