package liquibase.changelog;

import liquibase.ContextExpression;
import liquibase.Labels;

import java.util.Date;

public interface RanChangeSetFactory<T extends RanChangeSet> {
    T create(ChangeSet changeSet, ChangeSet.ExecType execType);

    T create(String fileName,
             String author,
             String id,
             String md5sum,
             String description,
             String comments,
             Date dateExecuted,
             Integer orderExecuted,
             String tag,
             String execType,
             ContextExpression contexts,
             Labels labels,
             String deploymentId);
}
