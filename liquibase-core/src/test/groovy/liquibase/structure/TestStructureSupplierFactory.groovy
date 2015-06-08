package liquibase.structure

import liquibase.Scope
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
        return obj.getPriority((Class<? extends DatabaseObject>) args[0], scope)
    }

    public AbstractTestStructureSupplier getTestStructureSupplier(Class<? extends DatabaseObject> type, Scope scope) {
        return getService(scope, type)
    }
}
