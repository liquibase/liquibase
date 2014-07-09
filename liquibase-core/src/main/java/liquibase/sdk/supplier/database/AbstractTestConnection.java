package liquibase.sdk.supplier.database;

import liquibase.database.Database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractTestConnection implements TestConnection {

    @Override
    public String toString() {
        return describe();
    }

    @Override
    public Database getConnectedDatabase() {
        Database database = getCorrectDatabase();
        database.setConnection(getConnection());
        database.getConnection().attached(database);

        return database;
    }



    @Override
    public List<String[]> getTestCatalogsAndSchemas() {
        Database db = getCorrectDatabase();
        List<String[]> returnList = new ArrayList<String[]>();

        returnList.add(new String[]{null, null});
        if (db.supportsSchemas()) {
            returnList.add(new String[]{null, "LBSCHEMA"});
            returnList.add(new String[]{"LBCAT", "LBSCHEMA"});
        }
        if (db.supportsCatalogs()) {
            returnList.add(new String[]{"LBCAT", null});
        }

        return returnList;
    }
}
