package liquibase.extension.testing.setup

import liquibase.changelog.ChangeSet


class HistoryEntry {
    public String id
    public String author
    public String path
    public ChangeSet.ExecType execType = ChangeSet.ExecType.EXECUTED
}
