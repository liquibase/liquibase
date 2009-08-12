package liquibase.database.structure.type;

public class UUIDType  extends DataType {
    @Override
    public String getDataTypeName() {
        return "CHAR(36)";
    }

}
