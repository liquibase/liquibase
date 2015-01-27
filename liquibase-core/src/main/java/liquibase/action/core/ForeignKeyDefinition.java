package liquibase.action.core;

import liquibase.AbstractExtensibleObject;

public class ForeignKeyDefinition extends AbstractExtensibleObject {

    public static enum Attr {
        foreignKeyName,
        columnNames,
        references,
        referencedTableName,
        referencedColumnNames,
        deleteCascade,
        initiallyDeferred,
        deferrable,

    }
}
