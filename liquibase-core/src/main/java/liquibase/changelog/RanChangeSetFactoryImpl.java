package liquibase.changelog;

import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.change.CheckSum;
import liquibase.logging.LogService;
import liquibase.logging.LogType;

import java.util.Date;

public class RanChangeSetFactoryImpl implements RanChangeSetFactory<RanChangeSet> {
    public RanChangeSet create(ChangeSet changeSet, ChangeSet.ExecType execType) {
        return new RanChangeSet(changeSet, execType, null, null);
    }

    public RanChangeSet create(String fileName, String author, String id, String md5sum, String description, String comments, Date dateExecuted, Integer orderExecuted, String tag, String execType, ContextExpression contexts, Labels labels, String deploymentId) {
        try {
            RanChangeSet ranChangeSet = new RanChangeSet(
                    fileName,
                    id,
                    author,
                    CheckSum.parse(md5sum),
                    dateExecuted,
                    tag,
                    ChangeSet.ExecType.valueOf(execType),
                    description,
                    comments,
                    contexts,
                    labels,
                    deploymentId);

            ranChangeSet.setOrderExecuted(orderExecuted);
            return ranChangeSet;
        } catch (IllegalArgumentException e) {
            LogService.getLog(getClass()).severe(LogType.LOG, "Unknown EXECTYPE from database: " + execType);
            throw e;
        }
    }
}
