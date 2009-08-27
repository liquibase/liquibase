package liquibase.database.structure.type;

public class DecimalType  extends DataType {
    public DecimalType() {
        super("DECIMAL",0,2);
    }

    public DecimalType(String dataTypeName) {
        super(dataTypeName,0,2);
    }

}
