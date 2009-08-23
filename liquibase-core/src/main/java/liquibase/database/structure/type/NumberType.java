package liquibase.database.structure.type;

import liquibase.database.structure.type.DataType;

public class NumberType extends DataType {
    @Override
    public String getDataTypeName() {
        return "NUMBER";
    }

    @Override
    public boolean getSupportsPrecision() {
        return true;
    }
}
