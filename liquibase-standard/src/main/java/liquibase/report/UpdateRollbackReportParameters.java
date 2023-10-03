package liquibase.report;

public interface UpdateRollbackReportParameters {
    RuntimeInfo getRuntimeInfo();

    OperationInfo getOperationInfo();

    CustomData getCustomData();
}
