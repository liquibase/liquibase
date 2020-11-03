package liquibase.hub.model;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class Operation implements HubModel {

    private UUID id;
    private Connection connection;
    private OperationStatus operationStatus;
    private Map<String, String> clientMetadata;
    private Map<String, String> parameters;
    private Date createDate;
    private Date removeDate;

    @Override
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public OperationStatus getOperationStatus() {
        return operationStatus;
    }

    public void setOperationStatus(OperationStatus operationStatus) {
        this.operationStatus = operationStatus;
    }

    public Map<String, String> getClientMetadata() {
        return clientMetadata;
    }

    public void setClientMetadata(Map<String, String> clientMetadata) {
        this.clientMetadata = clientMetadata;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getRemoveDate() {
        return removeDate;
    }

    public void setRemoveDate(Date removeDate) {
        this.removeDate = removeDate;
    }

    public static class OperationStatus {
        private UUID id;
        private String statusMessage;
        private String operationStatusTYpe;


        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getStatusMessage() {
            return statusMessage;
        }

        public void setStatusMessage(String statusMessage) {
            this.statusMessage = statusMessage;
        }
    }
}
