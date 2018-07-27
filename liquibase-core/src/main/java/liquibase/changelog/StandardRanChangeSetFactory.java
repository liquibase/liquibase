package liquibase.changelog;

import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.change.CheckSum;
import liquibase.logging.LogService;
import liquibase.logging.LogType;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class StandardRanChangeSetFactory implements RanChangeSetFactory<RanChangeSet> {

    @Override
    public RanChangeSet create(boolean databaseChecksumsCompatible, Map rs) {
        String fileName = rs.get("FILENAME").toString();
        String author = rs.get("AUTHOR").toString();
        String id = rs.get("ID").toString();
        String md5sum = ((rs.get("MD5SUM") == null) || !databaseChecksumsCompatible) ? null : rs.get("MD5SUM").toString();
        String description = (rs.get("DESCRIPTION") == null) ? null : rs.get("DESCRIPTION").toString();
        String comments = (rs.get("COMMENTS") == null) ? null : rs.get("COMMENTS").toString();
        Object tmpDateExecuted = rs.get("DATEEXECUTED");
        Date dateExecuted = null;
        if (tmpDateExecuted instanceof Date) {
            dateExecuted = (Date) tmpDateExecuted;
        } else {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                dateExecuted = df.parse((String) tmpDateExecuted);
            } catch (ParseException e) {
                // Ignore ParseException and assume dateExecuted == null instead of aborting.
            }
        }
        String tmpOrderExecuted = rs.get("ORDEREXECUTED").toString();
        Integer orderExecuted = ((tmpOrderExecuted == null) ? null : Integer.valueOf(tmpOrderExecuted));
        String tag = (rs.get("TAG") == null) ? null : rs.get("TAG").toString();
        String execType = (rs.get("EXECTYPE") == null) ? null : rs.get("EXECTYPE").toString();
        ContextExpression contexts = new ContextExpression((String) rs.get("CONTEXTS"));
        Labels labels = new Labels((String) rs.get("LABELS"));
        String deploymentId = (String) rs.get("DEPLOYMENT_ID");

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
