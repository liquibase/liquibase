package liquibase.report;

import lombok.Data;

@Data
public class OperationInfo {
    private String command;
    private String operationOutcome;
    private String operationOutcomeErrorMsg;
    private String exception;
}
