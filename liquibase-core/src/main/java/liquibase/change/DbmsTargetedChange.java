package liquibase.change;

/**
 * Adding this interface to your Change class allows you to specify a dbms attribute listing the databases the change is applied to.
 * This isn't in the default Change interface because normally the database targetting should be at the changeSet level since there is normally
 * one change per changeSet. However, for certain changes (especially raw sql) it makes sense to have just some changes within a changeset run per database.
 */
public interface DbmsTargetedChange {

    /**
     * @return A comma separated list of dbms' that this change will be run for. Will run for all dbms' if empty or null.
     */
    public String getDbms();

    public void setDbms(String dbms);

}
