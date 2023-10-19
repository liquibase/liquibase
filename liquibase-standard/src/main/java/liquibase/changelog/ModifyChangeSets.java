package liquibase.changelog;

/**
 * Container class to handle the modifyChangeSets tag.
 * Other attributes may be added later
 */
public class ModifyChangeSets {
    private final String runWith;
    private final String runWithSpool;

    /**
     * @param runWith      The native executor to execute all included change sets with. Can be null
     * @param runWithSpool The name of the spool file to be created
     */
    public ModifyChangeSets(String runWith, String runWithSpool) {
        this.runWith = runWith;
        this.runWithSpool = runWithSpool;
    }

    public String getRunWith() {
        return runWith;
    }

    public String getRunWithSpool() {
        return runWithSpool;
    }
}
