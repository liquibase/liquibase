package liquibase.database.example;

import liquibase.database.H2Database;
import liquibase.exception.JDBCException;

public class ExampleCustomDatabase extends H2Database {

    public String getProductName() {
        return "H2 Database: Custom Implementation";
    }

    public void tag(String tagString) throws JDBCException {
        System.out.println("Custom tagging");
        super.tag(tagString);
    }
}
