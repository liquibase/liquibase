package liquibase.changeset;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ModifyChangeSets;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.plugin.Plugin;

public interface ChangeSetService extends Plugin {
    int getPriority();
    ChangeSet createChangeSet(DatabaseChangeLog changeLog);
    ModifyChangeSets createModifyChangeSets(ParsedNode node) throws ParsedNodeException;
    void modifyChangeSets(ChangeSet changeSet, ModifyChangeSets modifyChangeSets);
}
