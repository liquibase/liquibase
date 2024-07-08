package liquibase.integration.ant.test

import groovy.sql.Sql
import org.apache.tools.ant.BuildException
import org.apache.tools.ant.ProjectComponent
import org.apache.tools.ant.taskdefs.condition.Condition

class LiquibaseTagExistsCondition extends ProjectComponent implements Condition {
    private String driver
    private String url
    private String user
    private String password
    private String tag

    @Override
    boolean eval() throws BuildException {
        Sql sql = null
        try {
            sql = Sql.newInstance(url, user, password, driver)
            def result = false
            sql.query("SELECT TAG FROM DATABASECHANGELOG WHERE TAG IS NOT NULL AND TAG = $tag;") {
                result = it.next()
            }
            return result
        } finally {
            sql?.close()
        }
    }

    void setDriver(String driver) {
        this.driver = driver
    }

    void setUrl(String url) {
        this.url = url
    }

    void setUser(String user) {
        this.user = user
    }

    void setPassword(String password) {
        this.password = password
    }

    void setTag(String tag) {
        this.tag = tag
    }
}
