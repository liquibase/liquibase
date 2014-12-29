package liquibase.action.core;

import liquibase.AbstractExtensibleObject;
import liquibase.action.ExecuteAction;
import liquibase.util.StringUtils;

public class CreateSequenceAction extends AbstractExtensibleObject implements ExecuteAction {

    public static enum Attributes {
        catalogName,
        schemaName,
        sequenceName,
        startValue,
        incrementBy,
        maxValue,
        minValue,
        ordered,
        cycle,
        cacheSize
    }

    @Override
    public String describe() {
        return "create sequence "+StringUtils.join(this, ",")+")";
    }
}
