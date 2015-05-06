package liquibase.structure;

import liquibase.structure.core.Table;

public class TestTableSupplier extends DefaultTestStructureSupplier {
    @Override
    protected Class<? extends DatabaseObject> getTypeCreates() {
        return Table;
    }


}
