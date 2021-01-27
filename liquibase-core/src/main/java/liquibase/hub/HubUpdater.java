package liquibase.hub;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.RuntimeEnvironment;
import liquibase.Scope;
import liquibase.changelog.ChangeLogIterator;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.ListVisitor;
import liquibase.changelog.visitor.RollbackListVisitor;
import liquibase.command.CommandFactory;
import liquibase.command.CommandResult;
import liquibase.command.core.SyncHubCommand;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.hub.model.*;
import liquibase.integration.IntegrationDetails;
import liquibase.logging.core.BufferedLogService;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

/**
 *
 * This class handles updating Hub during Liquibase operations
 *
 */
public class HubUpdater {
  private final Date startTime;
  private final DatabaseChangeLog changeLog;

  public HubUpdater(Date startTime, DatabaseChangeLog changeLog) {
    this.startTime = startTime;
    this.changeLog = changeLog;
  }

  /**
   *
   * This method performs a syncHub and returns a new Operation instance
   * If there is an error or the Hub is not available it returns null
   *
   * @param  operationType              Operation type (UPDATE or ROLLBACK)
   * @param  database                   Database object for connection
   * @param  connection                 Connection for this operation
   * @param  changeLogFile              Path to DatabaseChangelog for this operatoin
   * @param  contexts                   Contexts to use for filtering
   * @param  labelExpression            Labels to use for filtering
   * @param  changeLogIterator          Iterator to use for going through change sets
   * @return Operation                  Valid Operation object or null
   * @throws LiquibaseHubException      Thrown by HubService
   * @throws DatabaseException          Thrown by Liquibase core
   * @throws LiquibaseException         Thrown by Liquibase core
   *
   */
  public Operation preUpdateHub(String operationType,
                                Database database,
                                Connection connection,
                                String changeLogFile,
                                Contexts contexts,
                                LabelExpression labelExpression,
                                ChangeLogIterator changeLogIterator)
          throws LiquibaseHubException, DatabaseException, LiquibaseException, SQLException {
    if (hubIsNotAvailable(changeLog.getChangeLogId())) {
        return null;
    }

    //
    // Perform syncHub
    //
    syncHub(changeLogFile, database, changeLog, connection.getId());

    //
    // Load up metadata for database/driver version
    //
    loadDatabaseMetadata(database);

    //
    // Send the START operation event
    //
    final HubService hubService = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
    final HubChangeLog hubChangeLog = hubService.getHubChangeLog(UUID.fromString(changeLog.getChangeLogId()));
    Operation updateOperation = hubService.createOperation(operationType, hubChangeLog, connection);
    try {
        hubService.sendOperationEvent(updateOperation, new OperationEvent()
                .setEventType("START")
                .setStartDate(startTime)
                .setOperationEventStatus(
                        new OperationEvent.OperationEventStatus()
                                .setOperationEventStatusType("PASS")
                                .setStatusMessage("Update operation started successfully")
                )
      );
    } catch (LiquibaseException e) {
        Scope.getCurrentScope().getLog(getClass()).warning(e.getMessage(), e);
    }

    //
    // Send the list of change sets which will be updated/rolled back
    // If the operation type is DROPALL then we send no changes
    //
    ListVisitor listVisitor;
    if (operationType.toUpperCase().equals("ROLLBACK")) {
        listVisitor = new RollbackListVisitor();
    }
    else {
        listVisitor = new ListVisitor();
    }

    changeLogIterator.run(listVisitor, new RuntimeEnvironment(database, contexts, labelExpression));
    List<ChangeSet> operationChangeSets = listVisitor.getSeenChangeSets();
    OperationChange operationChange = new OperationChange();
    operationChange.getChangeSets().addAll(operationChangeSets);
    operationChange.setProject(hubChangeLog.getProject());
    operationChange.setOperation(updateOperation);
    try {
        hubService.sendOperationChanges(operationChange);
    } catch (LiquibaseException e) {
        Scope.getCurrentScope().getLog(getClass()).warning(e.getMessage(), e);
    }
    return updateOperation;
  }

  /**
   *
   * Update the Hub after the operation
   *
   * @param   updateOperation               Operation object used in the update
   * @param   bufferLog                     Log output
   *
   */
  public void postUpdateHub(Operation updateOperation, BufferedLogService bufferLog) {
    try {
      if (updateOperation == null || hubIsNotAvailable(changeLog.getChangeLogId())) {
        return;
      }

      //
      // Send the COMPLETE operation event
      //
      final HubService hubService = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
      hubService.sendOperationEvent(updateOperation, new OperationEvent()
                .setEventType("COMPLETE")
                .setStartDate(startTime)
                .setEndDate(new Date())
                .setOperationEventStatus(
                      new OperationEvent.OperationEventStatus()
                                        .setOperationEventStatusType("PASS")
                                        .setStatusMessage("Update operation completed successfully")
              )
              .setOperationEventLog(
                      new OperationEvent.OperationEventLog()
                                        .setLogMessage(bufferLog.getLogAsString(Level.INFO))
                                        .setTimestampLog(startTime)
              )
      );
    } catch (LiquibaseException e) {
        Scope.getCurrentScope().getLog(getClass()).warning(e.getMessage(), e);
    }
  }

  /**
   *
   * Handle Hub exceptions thrown during the operation
   *
   * @param updateOperation                    Operation object used during update
   * @param bufferLog                          Log output
   * @param originalExceptionMessage           Exception thrown by the operation
   *
   */
  public void postUpdateHubExceptionHandling(Operation updateOperation,
                                             BufferedLogService bufferLog,
                                             String originalExceptionMessage) {
    try {
      //
      // Not a valid Hub connection
      // Just go back
      //
      if (updateOperation == null || hubIsNotAvailable(changeLog.getChangeLogId())) {
        return;
      }
      final HubService hubService = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
      hubService.sendOperationEvent(updateOperation, new OperationEvent()
              .setEventType("COMPLETE")
              .setStartDate(startTime)
              .setEndDate(new Date())
              .setOperationEventStatus(
                      new OperationEvent.OperationEventStatus()
                              .setOperationEventStatusType("FAIL")
                              .setStatusMessage("Update operation completed with errors")
              )
              .setOperationEventLog(
                      new OperationEvent.OperationEventLog()
                              .setLogMessage(bufferLog.getLogAsString(Level.INFO))
              )
      );
    } catch (LiquibaseException serviceException) {
      Scope.getCurrentScope().getLog(getClass()).warning(originalExceptionMessage, serviceException);
    }
  }

  /**
   *
   * Determine if the Hub is not available
   *
   * @param   changeLogId    Changelog ID
   * @return  boolean
   *
   */
  public boolean hubIsNotAvailable(String changeLogId) {
    final HubService hubService = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
    return ! hubService.isOnline() || changeLogId == null;
  }

  public void syncHub(String changeLogFile, Database database, DatabaseChangeLog databaseChangeLog, UUID hubConnectionId) {
    final SyncHubCommand syncHub = (SyncHubCommand) CommandFactory.getInstance().getCommand("syncHub");
    syncHub.setChangeLogFile(changeLogFile);
    syncHub.setUrl(database.getConnection().getURL());
    syncHub.setHubConnectionId(hubConnectionId != null ? Objects.toString(hubConnectionId) : null);
    syncHub.setDatabase(database);
    syncHub.setFailIfOnline(false);

    try {
      syncHub.configure(Collections.singletonMap("changeLog", databaseChangeLog));
      final CommandResult commandResult = syncHub.execute();
      if (!commandResult.succeeded) {
        Scope.getCurrentScope().getLog(getClass()).warning("Liquibase Hub sync failed: " + commandResult.message);
      }
    } catch (Exception e) {
      Scope.getCurrentScope().getLog(getClass()).warning("Liquibase Hub sync failed: " + e.getMessage(), e);
    }
  }

  //
  // Put database/driver version information in the details map
  //
  private void loadDatabaseMetadata(Database database) throws DatabaseException, SQLException {
      if (database.getConnection() == null) {
        return;
      }
      final IntegrationDetails integrationDetails = Scope.getCurrentScope().get("integrationDetails", IntegrationDetails.class);
      if (integrationDetails == null) {
          return;
      }
      String databaseProductName = database.getDatabaseProductName();
      String databaseProductVersion = database.getDatabaseProductVersion();
      Scope.getCurrentScope().getLog(getClass()).fine("Database product name         " + databaseProductName);
      Scope.getCurrentScope().getLog(getClass()).fine("Database product version      " + databaseProductVersion);

      DatabaseConnection connection = database.getConnection();
      if (connection instanceof JdbcConnection) {
          JdbcConnection jdbcConnection = (JdbcConnection)connection;
          java.sql.Connection conn = jdbcConnection.getUnderlyingConnection();
          int driverMajorVersion = conn.getMetaData().getDriverMajorVersion();
          int driverMinorVersion = conn.getMetaData().getDriverMinorVersion();
          Scope.getCurrentScope().getLog(getClass()).fine("Database driver version       " +
                  Integer.toString(driverMajorVersion) + "." + Integer.toString(driverMinorVersion));
          integrationDetails.setParameter("db__driverVersion", Integer.toString(driverMajorVersion) + "." + Integer.toString(driverMinorVersion));
      }
      else {
          integrationDetails.setParameter("db__driverVersion", "Unable to determine");
      }
      integrationDetails.setParameter("db__databaseProduct", databaseProductName);
      integrationDetails.setParameter("db__databaseVersion", databaseProductVersion);
  }
}
