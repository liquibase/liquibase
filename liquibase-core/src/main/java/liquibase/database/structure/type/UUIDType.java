package liquibase.database.structure.type;

public class UUIDType  extends DataType {
    public UUIDType() {
        super("CHAR(36)");
    }

    public UUIDType(String dataTypeName) {
        super(dataTypeName);
    }
}
