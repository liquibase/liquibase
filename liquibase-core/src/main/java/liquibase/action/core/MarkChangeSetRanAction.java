package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.changelog.ChangeSet;

public class MarkChangeSetRanAction extends AbstractAction {
    public ChangeSet changeSet;
    public ChangeSet.ExecType execType;
}
