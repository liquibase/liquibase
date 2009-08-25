package liquibase.database.structure.type;

public class UnknownType extends DataType {
    private Boolean supportsPrecision;

    public UnknownType(String dataTypeName, Boolean supportsPrecision) {
        super(dataTypeName);
        this.supportsPrecision = supportsPrecision;
    }
}
