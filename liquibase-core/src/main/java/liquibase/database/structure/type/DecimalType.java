package liquibase.database.structure.type;

public class DecimalType  extends DataType {
    public DecimalType() {
        super("DECIMAL");
    }

    public DecimalType(String dataTypeName) {
        super(dataTypeName);
    }

}
