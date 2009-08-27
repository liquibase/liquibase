package liquibase.database.structure.type;

public class FloatType  extends DataType {
    public FloatType() {
        super("FLOAT",0,2);
    }

    public FloatType(String dataTypeName) {
        super(dataTypeName,0,2);
    }

}
