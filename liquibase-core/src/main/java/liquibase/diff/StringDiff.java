package liquibase.diff;

public class StringDiff {
    private String baseVersion;
    private String targetVersion;


    public StringDiff(String baseVersion, String targetVersion) {
        this.baseVersion = baseVersion;
        this.targetVersion = targetVersion;
    }


    public String getReferenceVersion() {
        return baseVersion;
    }

    public String getTargetVersion() {
        return targetVersion;
    }

    public boolean areEqual() {
        return baseVersion.equals(targetVersion);
    }
}
