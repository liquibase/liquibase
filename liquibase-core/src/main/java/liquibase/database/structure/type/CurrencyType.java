package liquibase.database.structure.type;

public class CurrencyType  extends DataType {
    public CurrencyType() {
        super("DECIMAL");
    }

    public CurrencyType(String dataTypeName) {
        super(dataTypeName);
    }

}
