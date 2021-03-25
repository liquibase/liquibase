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
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connectionStatus.connection));

        final ChangeLogHistoryService changeLogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
        changeLogService.init();
        changeLogService.generateDeploymentId();

        changeLogService.reset();
        CompositeResourceAccessor fileOpener = new CompositeResourceAccessor(
            new FileSystemResourceAccessor(Paths.get(".").toAbsolutePath().toFile()),
            new CommandLineResourceAccessor(getClass().getClassLoader())
        );
        Liquibase liquibase = new Liquibase(this.changeLog, fileOpener, database);
        liquibase.changeLogSync((String) null);
    }

    @Override
    public String getChangeLogFile() {
        return changeLog;
    }
}
