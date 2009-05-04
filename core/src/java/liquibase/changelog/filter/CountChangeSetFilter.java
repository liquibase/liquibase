package liquibase.changelog.filter;

import liquibase.ChangeSet;

public class CountChangeSetFilter implements ChangeSetFilter {

    private int changeSetsToAllow;
    private int changeSetsSeen = 0;

    public CountChangeSetFilter(int changeSetsToAllow) {
        this.changeSetsToAllow = changeSetsToAllow;
    }

    public boolean accepts(ChangeSet changeSet) {
        changeSetsSeen++;
        return changeSetsSeen <= changeSetsToAllow;
    }
}
