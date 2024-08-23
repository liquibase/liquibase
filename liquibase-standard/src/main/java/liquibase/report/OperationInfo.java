package liquibase.report;

import lombok.Data;

@Data
public class OperationInfo {
    private String command;
    private String operationOutcome;
    private String operationOutcomeErrorMsg;
    private String exception;
    private String updateSummaryMsg;
    private Integer rowsAffected;
    private Boolean rollbackOnError = Boolean.FALSE; // assume false unless set
    private String labels;
    private String contexts;
}
