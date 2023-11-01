package liquibase.report;

import java.util.Date;

public interface UpdateRollbackReportParameters {
    RuntimeInfo getRuntimeInfo();

    OperationInfo getOperationInfo();

    CustomData getCustomData();

    Date getDate();

    String getChangelogArgValue();

    String getJdbcUrl();

    String getCommandTitle();
}
