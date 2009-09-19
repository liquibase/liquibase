package liquibase.diff;

public class DiffComparison {
    private String baseVersion;
    private String targetVersion;


    public DiffComparison(String baseVersion, String targetVersion) {
        this.baseVersion = baseVersion;
        this.targetVersion = targetVersion;
    }


    public String getReferenceVersion() {
        return baseVersion;
    }

    public String getTargetVersion() {
        return targetVersion;
    }

    public boolean areTheSame() {
        return baseVersion.equals(targetVersion);
    }
}
