package liquibase.database.structure.type;

import liquibase.database.Database;

public class CharType extends TextType {

    public CharType() {
        super("CHAR",0,1);
    }

    public CharType(String dataTypeName) {
        super(dataTypeName,0,1);
    }

    @Override
    public boolean getSupportsPrecision() {
        return true;
    }
}
