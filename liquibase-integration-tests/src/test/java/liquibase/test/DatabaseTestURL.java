/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package liquibase.test;

import liquibase.dbtest.AbstractIntegrationTest;

/**
 *
 * @author lujop
 */
public class DatabaseTestURL {
    private String url;
    private String username;
    private String password;

    private String databaseManager;

    public DatabaseTestURL(String databaseManager, String defaultUrl) {
        // Use a specific URL / username / password if given in the test configuration properties.
        DatabaseTestURL configuredUrl = AbstractIntegrationTest.getDatabaseTestURL(databaseManager);
        if (configuredUrl == null) {
            this.url = defaultUrl;
        } else {
            this.url = configuredUrl.getUrl();
            this.username = configuredUrl.getUsername();
            this.password = configuredUrl.getPassword();
        }
        this.databaseManager = databaseManager;
    }

    public DatabaseTestURL(String databaseManager, String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.databaseManager = databaseManager;
    }

    public String getDatabaseManager() {
        return databaseManager;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
