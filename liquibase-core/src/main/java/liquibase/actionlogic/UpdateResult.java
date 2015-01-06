package liquibase.actionlogic;

/**
 * Result of an action that updates existing data.
 */
public class UpdateResult extends ActionResult {

    private long numberAffected;

    public UpdateResult(long numberAffected) {
        this.numberAffected = numberAffected;
    }

    public UpdateResult(long numberAffected, String message) {
        super(message);
        this.numberAffected = numberAffected;
    }

    /**
     * Returns the number of items (such as rows) updated.
     */
    public long getNumberAffected() {
        return numberAffected;
    }
}
