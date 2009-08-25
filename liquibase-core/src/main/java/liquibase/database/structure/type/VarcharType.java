package liquibase.database.structure.type;

public class VarcharType extends CharType {

    public VarcharType() {
        super("VARCHAR");
    }

    public VarcharType(String dataTypeName) {
        super(dataTypeName);
    }
}
