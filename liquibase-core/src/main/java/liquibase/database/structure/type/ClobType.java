package liquibase.database.structure.type;

public class ClobType extends DataType {

    public ClobType() {
        super("CLOB");
    }

    public ClobType(String dataTypeName) {
        super(dataTypeName);
    }
}
