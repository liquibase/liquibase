package liquibase.structure

import liquibase.structure.core.Index

public class TestIndexSupplier extends DefaultTestStructureSupplier {
    @Override
    protected Class<? extends DatabaseObject> getTypeCreates() {
        return Index;
    }
}
