package liquibase.database.structure.type;

public class DoubleType  extends DataType {
    public DoubleType() {
        super("DOUBLE",0,2);
    }

    public DoubleType(String dataTypeName) {
        super(dataTypeName,0,0);
    }

}
