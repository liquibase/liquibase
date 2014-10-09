package liquibase.integration.ant.test

import groovy.sql.Sql
import org.apache.tools.ant.BuildException
import org.apache.tools.ant.ProjectComponent
import org.apache.tools.ant.taskdefs.condition.Condition

class RowCountEqualsCondition extends ProjectComponent implements Condition {
    private String driver
    private String url
    private String user
    private String password
    private String table
    private int count;

    @Override
    boolean eval() throws BuildException {
        Sql sql = null
        try {
            sql = Sql.newInstance(url, user, password, driver)
            def result = sql.firstRow("SELECT COUNT(*) AS the_count FROM $table;".toString())
            int actual = result.get("the_count") as int
            return count == actual
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

    void setTable(String table) {
        this.table = table
    }

    void setCount(int count) {
        this.count = count
    }
}
