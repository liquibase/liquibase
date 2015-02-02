package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.changelog.ChangeSet;

public class MarkChangeSetRanAction extends AbstractAction {
    public static enum Attr {
        changeSet,
        execType,
    }
}
