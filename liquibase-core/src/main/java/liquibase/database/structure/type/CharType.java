package liquibase.database.structure.type;

public class CharType extends DataType {
    @Override
    public String getDataTypeName() {
        return "CHAR";
    }

    public boolean getSupportsPrecision() {
        return true;
    }
}
