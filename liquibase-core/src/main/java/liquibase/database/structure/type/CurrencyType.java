package liquibase.database.structure.type;

public class CurrencyType  extends DataType {
    @Override
    public String getDataTypeName() {
        return "DECIMAL";
    }

}
