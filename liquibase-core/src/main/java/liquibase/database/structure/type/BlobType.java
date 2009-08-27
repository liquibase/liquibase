package liquibase.database.structure.type;

public class BlobType extends DataType {
    public BlobType() {
        super("BLOB",0,0);
    }

    public BlobType(String dataTypeName) {
        super(dataTypeName,0,0);
    }

}
