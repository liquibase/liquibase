package liquibase.report;

import liquibase.changelog.ChangeSet;
import liquibase.util.CollectionUtil;
import liquibase.util.NetUtil;
import liquibase.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class RollbackReportParameters implements UpdateRollbackReportParameters {
    private String changelogArgValue;
    private String jdbcUrl;
    private String commandTitle = "Rollback";
    private final DatabaseInfo databaseInfo = new DatabaseInfo();
    private final RuntimeInfo runtimeInfo = new RuntimeInfo();
    private final OperationInfo operationInfo = new OperationInfo();
    private final CustomData customData = new CustomData();
    private final ChangesetInfo changesetInfo = new ChangesetInfo();
    private final Date date = new Date();
}
