package liquibase.report;

import liquibase.changelog.ChangeSet;
import liquibase.util.CollectionUtil;
import liquibase.util.StringUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ChangesetInfo {
    private int changesetCount;
    private final List<IndividualChangesetInfo> changesetInfoList = new ArrayList<>();

    public void addAllToChangesetInfoList(List<ChangeSet> changeSets, boolean isRollback) {
        if (changeSets != null) {
            for (ChangeSet deployedChangeSet : changeSets) {
                String changesetOutcome;
                if (isRollback) {
                    changesetOutcome = deployedChangeSet.getRollbackExecType() == null ? "" : deployedChangeSet.getRollbackExecType().toString();
                } else {
                    changesetOutcome = deployedChangeSet.getExecType() == null ? "" : deployedChangeSet.getExecType().toString();
                }
                boolean success = true;
                // If the changeset fails, the exec type it has is null, but if there's an error message, then it failed, and we want to indicate that it failed.
                String errorMsg = deployedChangeSet.getErrorMsg();
                if (StringUtil.isNotEmpty(errorMsg)) {
                    changesetOutcome = ChangeSet.ExecType.FAILED.value;
                    success = false;
                }
                List<String> generatedSql = deployedChangeSet.getGeneratedSql()
                        .stream()
                        .filter(sql -> sql != null && !sql.isEmpty())
                        .collect(Collectors.toList());
                changesetInfoList.add(new IndividualChangesetInfo(
                        changesetInfoList.size() + 1,
                        deployedChangeSet.getAuthor(),
                        deployedChangeSet.getId(),
                        deployedChangeSet.getFilePath(),
                        deployedChangeSet.getComments(),
                        success,
                        changesetOutcome,
                        errorMsg,
                        deployedChangeSet.getLabels() == null ? null : deployedChangeSet.getLabels().toString(),
                        deployedChangeSet.getContextFilter() == null ? null : deployedChangeSet.getContextFilter().getOriginalString(),
                        buildAttributesString(deployedChangeSet),
                        generatedSql
                ));
            }
        }
    }

    private static List<String> buildAttributesString(ChangeSet changeSet) {
        List<String> attributes = new ArrayList<>();

        if (changeSet.getFailOnError() != null && !changeSet.getFailOnError()) {
            attributes.add("failOnError = false");
        }
        if (changeSet.isAlwaysRun()) {
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
