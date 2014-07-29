package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;

import java.util.List;

public abstract class RanChangeSetFilter implements ChangeSetFilter {
    public List<RanChangeSet> ranChangeSets;
    private final boolean ignoreClasspathPrefix;

    public RanChangeSetFilter(List<RanChangeSet> ranChangeSets, boolean ignoreClasspathPrefix) {
        this.ignoreClasspathPrefix = ignoreClasspathPrefix;
        this.ranChangeSets = ranChangeSets;
    }

    public RanChangeSet getRanChangeSet(ChangeSet changeSet) {
        for (RanChangeSet ranChangeSet : ranChangeSets) {
            if (ranChangeSet.getId().equalsIgnoreCase(changeSet.getId())
                    && ranChangeSet.getAuthor().equalsIgnoreCase(changeSet.getAuthor())
                    && normalizePath(ranChangeSet.getChangeLog()).equalsIgnoreCase(normalizePath(changeSet.getFilePath()))) {
                return ranChangeSet;
            }
        }
        return null;

    }
    protected String normalizePath(String filePath) {
        if (ignoreClasspathPrefix) {
            return filePath.replaceFirst("^classpath:", "");
        }
        return filePath;
    }
}
