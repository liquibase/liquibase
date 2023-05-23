package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;

public class CountChangeSetFilter implements ChangeSetFilter {

    private final int changeSetsToAllow;
    private int changeSetsSeen;

    public CountChangeSetFilter(int changeSetsToAllow) {
        this.changeSetsToAllow = changeSetsToAllow;
    }

    @Override
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
        changeSetsSeen++;

        if (changeSetsSeen <= changeSetsToAllow) {
            return new ChangeSetFilterResult(true, "One of "+changeSetsToAllow+" changesets to run", this.getClass(), getMdcName(), getDisplayName());
        } else {
            String plurality = "changesets";
            if (changeSetsToAllow == 1) {
                plurality = "changeset";
            }
            return new ChangeSetFilterResult(false, "Only running "+changeSetsToAllow+" " + plurality, this.getClass(), getMdcName(), getDisplayName());
        }
    }

    @Override
    public String getMdcName() {
        return "afterCount";
    }

    @Override
    public String getDisplayName() {
        return "After count";
    }
}
