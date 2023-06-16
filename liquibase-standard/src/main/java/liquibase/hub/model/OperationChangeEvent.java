package liquibase.hub.model;

import java.util.Date;
import java.util.UUID;

public class OperationChangeEvent implements HubModel {
    private String eventType;
    private Date startDate;
    private Date endDate;
    private Date dateExecuted;
    private UUID hubChangeId;
    private String changesetId;
    private String changesetAuthor;
    private String changesetFilename;
    private String[] generatedSql;
    private String changesetBody;
    private String operationStatusType;
    private String statusMessage;
    private String logs;
    private Date logsTimestamp;
    private Project project;
    private Operation operation;

    @Override
    public UUID getId() {
        return null;
    }

    public Project getProject() {
        return project;
    }

    public OperationChangeEvent setProject(Project project) {
        this.project = project;
        return this;
    }

    public Operation getOperation() {
        return operation;
    }

    public OperationChangeEvent setOperation(Operation operation) {
        this.operation = operation;
        return this;
    }

    public Date getStartDate() {
        return startDate;
    }

    public OperationChangeEvent setStartDate(Date startDate) {
        this.startDate = startDate;
        return this;
    }

    public Date getEndDate() {
        return endDate;
    }

    public OperationChangeEvent setEndDate(Date endDate) {
        this.endDate = endDate;
        return this;
    }

    public Date getDateExecuted() {
        return dateExecuted;
    }

    public OperationChangeEvent setDateExecuted(Date dateExecuted) {
        this.dateExecuted = dateExecuted;
        return this;
    }

    public UUID getHubChangeId() {
        return hubChangeId;
    }

    public OperationChangeEvent setHubChangeId(UUID hubChangeId) {
        this.hubChangeId = hubChangeId;
        return this;
    }

    public String getChangesetId() {
        return changesetId;
    }

    public OperationChangeEvent setChangesetId(String changesetId) {
        this.changesetId = changesetId;
        return this;
    }

    public String getChangesetAuthor() {
        return changesetAuthor;
    }

    public OperationChangeEvent setChangesetAuthor(String changesetAuthor) {
        this.changesetAuthor = changesetAuthor;
        return this;
    }

    public String getChangesetFilename() {
        return changesetFilename;
    }

    public OperationChangeEvent setChangesetFilename(String changesetFilename) {
      this.changesetFilename = changesetFilename;
      return this;
    }

    public String[] getGeneratedSql() {
      return generatedSql;
    }

    public OperationChangeEvent setGeneratedSql(String[] generatedSql) {
        this.generatedSql = generatedSql;
        return this;
    }

    public String getChangesetBody() {
        return changesetBody;
    }

    public OperationChangeEvent setChangesetBody(String changesetBody) {
        this.changesetBody = changesetBody;
        return this;
    }

    public String getOperationStatusType() {
        return operationStatusType;
    }

    public OperationChangeEvent setOperationStatusType(String operationStatusType) {
        this.operationStatusType = operationStatusType;
        return this;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public OperationChangeEvent setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
        return this;
    }

    public String getLogs() {
        return logs;
    }

    public OperationChangeEvent setLogs(String logs) {
        this.logs = logs;
        return this;
    }

    public Date getLogsTimestamp() {
        return logsTimestamp;
    }

    public OperationChangeEvent setLogsTimestamp(Date logsTimestamp) {
        this.logsTimestamp = logsTimestamp;
        return this;
    }

    public String getEventType() {
    return eventType;
  }

    public OperationChangeEvent setEventType(String eventType) {
        this.eventType = eventType;
        return this;
    }
}
