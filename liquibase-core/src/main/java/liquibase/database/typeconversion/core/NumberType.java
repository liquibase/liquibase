package liquibase.database.typeconversion.core;

import liquibase.database.structure.type.DataType;

public class NumberType extends DataType {
    @Override
    public String getDataTypeName() {
        return "NUMBER";
    }

}
