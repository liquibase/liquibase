package liquibase.sdk.supplier.database;

import liquibase.database.Database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

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

            for (String cat : getAvailableCatalogs()) {
                for (String schema : getAvailableSchemas()) {
                    returnList.add(new String[]{cat, schema});
                }
            }

        ListIterator<String[]> iterator = returnList.listIterator();
        while(iterator.hasNext()) {
            String[] value = iterator.next();
            if (!db.supportsSchemas() && value[0] != null && value[1] != null && value[0].equalsIgnoreCase(value[1])) {
                iterator.remove();
            }
        }

        return returnList;
    }

    public List<String> getAvailableCatalogs() {
        List<String> returnList = new ArrayList<String>();
        returnList.add(null);
        if (getConnectedDatabase().supportsCatalogs()) {
            returnList.add("LBCAT");
//            returnList.add("LBCAT2");
        }
        return returnList;
    }

    public List<String> getAvailableSchemas() {
        List<String> returnList = new ArrayList<String>();
        returnList.add(null);
        if (getConnectedDatabase().supportsSchemas()) {
            returnList.add("LBSCHEMA");
            returnList.add("LBSCHEMA2");
        }
        return returnList;
    }
}
