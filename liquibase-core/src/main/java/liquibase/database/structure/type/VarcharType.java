package liquibase.database.structure.type;

public class VarcharType extends DataType {

    @Override
    public String getDataTypeName() {
        return "VARCHAR";
    }
    public boolean getSupportsPrecision() {
        return true;
    }
}
