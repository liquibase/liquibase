package liquibase.extension.testing.setup


import liquibase.changelog.ChangelogRewriter

class SetupModifyChangelog extends TestSetup {

    private final String changeLogFile
    private final String id

    SetupModifyChangelog(String changeLogFile) {
        this.changeLogFile = changeLogFile
    }

    SetupModifyChangelog(String changeLogFile, String id) {
        this.changeLogFile = changeLogFile
        this.id = id
    }

    @Override
    void setup(TestSetupEnvironment testSetupEnvironment) throws Exception {
        ChangelogRewriter.addChangeLogId(changeLogFile, id, null)
    }
}
