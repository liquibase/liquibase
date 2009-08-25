package liquibase.database.structure.type;

import liquibase.database.structure.type.DataType;

public class NumberType extends DataType {
    public NumberType() {
        super("NUMBER");
    }

    public NumberType(String dataTypeName) {
        super(dataTypeName);
    }

}
