package liquibase.database.structure.type;

public class BlobType extends DataType {
    public BlobType() {
        super("BLOB");
    }

    public BlobType(String dataTypeName) {
        super(dataTypeName);
    }

}
