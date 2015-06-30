package liquibase.action.core;

import liquibase.AbstractExtensibleObject;
import liquibase.structure.ObjectName;
import liquibase.util.StringClauses;

import java.util.List;

public class ForeignKeyDefinition extends AbstractExtensibleObject {

    public String foreignKeyName;
    public List<String> columnNames;
    public StringClauses references;

    public ObjectName referencedTableName;
    public List<String> referencedColumnNames;
    public Boolean deleteCascade;
    public Boolean initiallyDeferred;
    public Boolean deferrable;
}
