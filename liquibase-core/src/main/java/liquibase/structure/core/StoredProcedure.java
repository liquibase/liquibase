package liquibase.structure.core;

public class StoredProcedure extends StoredDatabaseLogic<StoredProcedure> {

    public StoredProcedure() {
    }

    public StoredProcedure(String catalogName, String schemaName, String procedureName) {
        this.setSchema(new Schema(catalogName, schemaName));
        setName(procedureName);
    }

}
