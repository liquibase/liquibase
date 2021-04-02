package liquibase.integrationtest.setup;


import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.integration.commandline.CommandLineResourceAccessor;
import liquibase.integrationtest.TestDatabaseConnections;
import liquibase.integrationtest.TestSetup;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.InputStreamList;
import liquibase.resource.ResourceAccessor;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

public class SetupDatabaseChangeLog extends TestSetup {

    private final String changeLog;

    public SetupDatabaseChangeLog(String changeLog) {
        this.changeLog = changeLog;
    }

    @Override
    public void setup(TestDatabaseConnections.ConnectionStatus connectionStatus) throws Exception {
    }

    @Override
    public String getChangeLogFile() {
        return changeLog;
    }
}
