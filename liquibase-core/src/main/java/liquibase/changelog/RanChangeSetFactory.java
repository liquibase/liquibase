package liquibase.changelog;

import java.util.Map;

public interface RanChangeSetFactory<T extends RanChangeSet> {

    T create(boolean databaseIsCheckSumCompatible, Map rs);
}
