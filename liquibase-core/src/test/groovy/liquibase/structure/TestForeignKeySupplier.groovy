package liquibase.structure

import liquibase.structure.core.ForeignKey

public class TestForeignKeySupplier extends DefaultTestStructureSupplier {
    @Override
    protected Class<? extends DatabaseObject> getTypeCreates() {
        return ForeignKey;
    }
}
