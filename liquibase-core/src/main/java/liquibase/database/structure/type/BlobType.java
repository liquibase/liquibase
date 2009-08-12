package liquibase.database.structure.type;

public class BlobType extends DataType {
    @Override
    public String getDataTypeName() {
        return "BLOB";
    }
}
