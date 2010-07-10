package liquibase.database.structure.type;

public class NVarcharType extends CharType {

    public NVarcharType() {
        super("NVARCHAR");
    }

    public NVarcharType(String dataTypeName) {
        super(dataTypeName);
    }
}