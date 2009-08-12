package liquibase.database.structure.type;

public class DecimalType  extends DataType {
    @Override
    public String getDataTypeName() {
        return "DECIMAL";
    }

}
