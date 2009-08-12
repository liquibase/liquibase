package liquibase.database.structure.type;

public class UnknownType extends DataType {
    private String dataTypeName;
    private Boolean supportsPrecision;

    public UnknownType(String dataTypeName, Boolean supportsPrecision) {
        this.dataTypeName = dataTypeName;
        this.supportsPrecision = supportsPrecision;
    }

    @Override
    public String getDataTypeName() {
        return dataTypeName;
    }
}
