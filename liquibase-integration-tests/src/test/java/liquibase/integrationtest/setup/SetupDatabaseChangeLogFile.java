package liquibase.integrationtest.setup;


import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.integrationtest.TestDatabaseConnections;
import liquibase.integrationtest.TestSetup;

import java.io.File;
import java.io.IOException;

public class SetupDatabaseChangeLogFile extends TestSetup {

    private String changeLog;

    public SetupDatabaseChangeLogFile(String changeLog) {
        if (changeLog != null) {
            this.changeLog = changeLog;
            return;
        }
        try {
            File f = File.createTempFile("changeLog-", ".xml", new File("target/test-classes"));
            this.changeLog = "target/test-classes/" + f.getName();
        }
        catch (IOException ioe) {
            // consume for now
        }
    }

    @Override
    public void setup(TestDatabaseConnections.ConnectionStatus connectionStatus) throws Exception {
    }

    @Override
    public String getChangeLogFile() {
        return changeLog;
    }
}
