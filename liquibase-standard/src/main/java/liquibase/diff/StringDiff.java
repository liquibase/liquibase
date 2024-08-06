package liquibase.diff;

import lombok.Getter;

public class StringDiff {
    private final String baseVersion;
    @Getter
    private final String targetVersion;


    public StringDiff(String baseVersion, String targetVersion) {
        this.baseVersion = baseVersion;
        this.targetVersion = targetVersion;
    }


    public String getReferenceVersion() {
        return baseVersion;
    }

    public boolean areEqual() {
        if (baseVersion == null) {
            return targetVersion == null;
        }
        return baseVersion.equals(targetVersion);
    }
}
