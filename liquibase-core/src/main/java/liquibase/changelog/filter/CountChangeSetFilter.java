package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;

public class CountChangeSetFilter implements ChangeSetFilter {

    private int changeSetsToAllow;
    private int changeSetsSeen;

    public CountChangeSetFilter(int changeSetsToAllow) {
        this.changeSetsToAllow = changeSetsToAllow;
    }

    @Override
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
        changeSetsSeen++;

        if (changeSetsSeen <= changeSetsToAllow) {
            return new ChangeSetFilterResult(true, "One of "+changeSetsToAllow+" change sets to run", this.getClass());
        } else {
            return new ChangeSetFilterResult(false, "Only running "+changeSetsToAllow+" change sets", this.getClass());
        }
    }
}
