package liquibase.structure

import liquibase.Scope
import liquibase.database.ConnectionSupplier
import liquibase.servicelocator.AbstractServiceFactory

class TestStructureSupplierFactory extends AbstractServiceFactory<AbstractTestStructureSupplier> {

    TestStructureSupplierFactory(Scope scope) {
        super(scope)
    }

    @Override
    protected Class<AbstractTestStructureSupplier> getServiceClass() {
        return AbstractTestStructureSupplier.class;
    }

    @Override
    protected int getPriority(AbstractTestStructureSupplier obj, Scope scope, Object... args) {
        return obj.getPriority((Class<? extends DatabaseObject>) args[0], (ConnectionSupplier) args[1], scope)
    }

    public AbstractTestStructureSupplier getTestStructureSupplier(Class<? extends DatabaseObject> type, ConnectionSupplier supplier, Scope scope) {
        return getService(scope, type, supplier)
    }
}
