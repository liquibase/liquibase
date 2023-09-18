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
public class UpdateReportParameters {
    private String changelogArgValue;
    private String jdbcUrl;
    private final DatabaseInfo databaseInfo = new DatabaseInfo();
    private final RuntimeInfo runtimeInfo = new RuntimeInfo();
    private final OperationInfo operationInfo = new OperationInfo();
    private final CustomData customData = new CustomData();
    private final ChangesetInfo changesetInfo = new ChangesetInfo();
    private final Date date = new Date();

    @Data
    public static class DatabaseInfo {
        private String databaseType;
        private String version;
    }

    @Data
    public static class RuntimeInfo {
        private final String systemUsername = System.getProperty("user.name");
        private final String hostname = NetUtil.getLocalHostName();
        private final String os = System.getProperty("os.name");
        private String interfaceType;
        private String startTime;
    }

    @Data
    public static class OperationInfo {
        private String command;
        private String operationOutcome;
        private String operationOutcomeErrorMsg;
    }

    @Data
    public static class CustomData {
        private String customDataFile;
        private Map<String, Object> fileContents;
    }

    @Data
    public static class ChangesetInfo {
        private int changesetCount;
        private final List<IndividualChangesetInfo> changesetInfoList = new ArrayList<>();

        public void addAllToChangesetInfoList(List<ChangeSet> changeSets) {
            if (changeSets != null) {
                for (ChangeSet deployedChangeSet : changeSets) {
                    String changesetOutcome = deployedChangeSet.getExecType() == null ? "" : deployedChangeSet.getExecType().toString();
                    // If the changeset fails, the exec type it has is null, but if there's an error message, then it failed, and we want to indicate that it failed.
                    String errorMsg = deployedChangeSet.getErrorMsg();
                    if (StringUtil.isNotEmpty(errorMsg)) {
                        changesetOutcome = ChangeSet.ExecType.FAILED.value;
                    }
                    changesetInfoList.add(new IndividualChangesetInfo(
                            changesetInfoList.size() + 1,
                            deployedChangeSet.getAuthor(),
                            deployedChangeSet.getId(),
                            deployedChangeSet.getFilePath(),
                            deployedChangeSet.getComments(),
                            changesetOutcome,
                            errorMsg,
                            deployedChangeSet.getLabels() == null ? null : deployedChangeSet.getLabels().toString(),
                            deployedChangeSet.getContextFilter() == null ? null : deployedChangeSet.getContextFilter().getOriginalString(),
                            buildAttributesString(deployedChangeSet),
                            deployedChangeSet.getGeneratedSql()
                    ));
                }
            }
        }
    }

    @Data
    @AllArgsConstructor
    public static class IndividualChangesetInfo {
        private int index;
        private String changesetAuthor;
        private String changesetId;
        private String changelogFile;
        private String comment;
        private String changesetOutcome;
        private String errorMsg;
        private String labels;
        private String contexts;
        private List<String> attributes;
        private List<String> generatedSql;

        /**
         * Used in the report template. Do not remove.
         * @return true if there are any attributes
         */
        public boolean hasAttributes() {
            return !CollectionUtil.createIfNull(attributes).isEmpty();
        }
    }

    private static List<String> buildAttributesString(ChangeSet changeSet) {
        List<String> attributes = new ArrayList<>();

        if (changeSet.getFailOnError() != null && !changeSet.getFailOnError()) {
            attributes.add("failOnError = false");
        }
        if (changeSet.isAlwaysRun()){
            attributes.add("alwaysRun");
        }
        if (changeSet.isRunOnChange()) {
            attributes.add("runOnChange");
        }
        if (!changeSet.isRunInTransaction()) {
            attributes.add("runInTransaction = false");
        }
        if (StringUtil.isNotEmpty(changeSet.getRunOrder())) {
            attributes.add("runOrder = " + changeSet.getRunOrder());
        }
        if (StringUtil.isNotEmpty(changeSet.getRunWith())) {
            attributes.add("runWith = " + changeSet.getRunWith());
        }
        if (StringUtil.isNotEmpty(changeSet.getRunWithSpoolFile())) {
            attributes.add("runWithSpoolFile = " + changeSet.getRunWithSpoolFile());
        }
        if (!CollectionUtil.createIfNull(changeSet.getDbmsSet()).isEmpty()) {
            attributes.add("dbms = " + StringUtil.join(changeSet.getDbmsSet(), ", "));
        }
        return attributes;
    }
}
