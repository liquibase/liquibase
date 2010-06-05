/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package liquibase.test;

/**
 *
 * @author lujop
 */
public class DatabaseTestURL {
    private String url;
    private String databaseManager;

    public DatabaseTestURL(String databaseManager,String url) {
        this.url = url;
        this.databaseManager = databaseManager;
    }

    public String getDatabaseManager() {
        return databaseManager;
    }

    public String getUrl() {
        return url;
    }

}
