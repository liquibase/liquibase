package liquibase.integrationtest.setup;


import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.integrationtest.TestDatabaseConnections;
import liquibase.integrationtest.TestSetup;
import liquibase.resource.InputStreamList;
import liquibase.resource.ResourceAccessor;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;

public class SetupDatabaseChangeLog extends TestSetup {

    private final String changeLog;

    public SetupDatabaseChangeLog(String changeLog) {
        this.changeLog = changeLog;
    }

    @Override
    public void setup(TestDatabaseConnections.ConnectionStatus connectionStatus) throws Exception {
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connectionStatus.connection));

        final ChangeLogHistoryService changeLogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
        changeLogService.init();
        changeLogService.generateDeploymentId();

        changeLogService.reset();
    }

    @Override
    public String getChangeLogFile() {
        try {
            URL url = Thread.currentThread().getContextClassLoader().getResource(changeLog);
            return url.getFile();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
