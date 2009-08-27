package liquibase.database.structure.type;

public class ClobType extends DataType {

    public ClobType() {
        super("CLOB",0,0);
    }

    public ClobType(String dataTypeName) {
        super(dataTypeName,0,0);
    }
}
