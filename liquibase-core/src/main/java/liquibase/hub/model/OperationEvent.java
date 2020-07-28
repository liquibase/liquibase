package liquibase.hub.model;

import java.util.Date;
import java.util.UUID;

public class OperationEvent implements HubModel {

    private UUID id;
    private String type;
    private Operation operation;
    private Date startDate;
    private Date endDate;
    private int statusCode;
    private String statusMessage;
    private String logs;

    public OperationEvent(String type, Operation operation, Date startDate, Date endDate, int statusCode, String statusMessage, String logs) {
        this.type = type;
        this.operation = operation;
        this.startDate = startDate;
        this.endDate = endDate;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.logs = logs;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getLogs() {
        return logs;
    }

    public void setLogs(String logs) {
        this.logs = logs;
    }
}
