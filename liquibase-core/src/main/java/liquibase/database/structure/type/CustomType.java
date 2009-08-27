package liquibase.database.structure.type;

public class CustomType extends DataType {

    public CustomType(String dataTypeName, int minParams, int maxParams) {
        super(dataTypeName, 0, 0);
    }
}
