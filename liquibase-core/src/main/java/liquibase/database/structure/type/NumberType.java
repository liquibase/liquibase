package liquibase.database.structure.type;

import liquibase.database.structure.type.DataType;

public class NumberType extends DataType {
    public NumberType() {
        super("NUMBER",0,2);
    }

    public NumberType(String dataTypeName) {
        super(dataTypeName,0,2);
    }

}
