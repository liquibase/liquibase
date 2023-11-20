package liquibase.report;

import liquibase.changelog.ChangeSet;
import liquibase.util.CollectionUtil;
import liquibase.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class UpdateReportParameters implements UpdateRollbackReportParameters {
    private String changelogArgValue;
    private String jdbcUrl;
    private String tag;
    private String commandTitle = "Update";
    private Boolean success = Boolean.TRUE; // assume success until we know we failed
    private final DatabaseInfo databaseInfo = new DatabaseInfo();
    private final RuntimeInfo runtimeInfo = new RuntimeInfo();
    private final OperationInfo operationInfo = new OperationInfo();
    private final CustomData customData = new CustomData();
    private final ChangesetInfo changesetInfo = new ChangesetInfo();
    private final Date date = new Date();
}
