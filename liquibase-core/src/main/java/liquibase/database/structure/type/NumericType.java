package liquibase.database.structure.type;

public class NumericType extends NumberType {
    public NumericType() {
        this("NUMERIC");
    }

    public NumericType(String dataTypeName) {
        super(dataTypeName);
    }

}
