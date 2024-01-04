package liquibase.change;

/**
 * ReplaceIfExists interface will control whether an implementation change object class will set the replaceIfExists flag that basically will tell
 * generate-changelog/diffToChangelog commands the given change object needs to generate the SQL for replace the stored logic if it already exists
 */
public interface ReplaceIfExists {

    void setReplaceIfExists(Boolean flag);

}
