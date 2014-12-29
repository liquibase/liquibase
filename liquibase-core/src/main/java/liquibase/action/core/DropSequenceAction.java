package liquibase.action.core;

import liquibase.AbstractExtensibleObject;
import liquibase.action.ExecuteAction;
import liquibase.util.StringUtils;

public class DropSequenceAction extends AbstractExtensibleObject implements ExecuteAction {

    public static enum Attributes {
        catalogName,
        schemaName,
        sequenceName,
    }

    @Override
    public String describe() {
        return "drop sequence "+StringUtils.join(this, ",")+")";
    }
}
