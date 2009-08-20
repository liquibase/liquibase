package liquibase.database.structure.type;

public class DateTimeType extends DataType {
    @Override
    public String getDataTypeName() {
        return "DATETIME";
    }
    public boolean getSupportsPrecision() {
        return true;
    }
}
