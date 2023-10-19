package liquibase.changeset;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ModifyChangeSets;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;

public class StandardChangeSetService implements ChangeSetService {
    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public ChangeSet createChangeSet(DatabaseChangeLog changeLog) {
        return new ChangeSet(changeLog);
    }

    @Override
    public ModifyChangeSets createModifyChangeSets(ParsedNode node) throws ParsedNodeException {
        return new ModifyChangeSets(
                (String) node.getChildValue(null, "runWith"),
                (String) node.getChildValue(null, "runWithSpoolFile"));
    }

    @Override
    public void modifyChangeSets(ChangeSet changeSet, ModifyChangeSets modifyChangeSets) {
        if (changeSet.getRunWith() == null) {
            changeSet.setRunWith(modifyChangeSets != null ? modifyChangeSets.getRunWith() : null);
        }
        if (changeSet.getRunWithSpoolFile() == null) {
            changeSet.setRunWithSpoolFile(modifyChangeSets != null ? modifyChangeSets.getRunWithSpool() : null);
        }
    }
}
