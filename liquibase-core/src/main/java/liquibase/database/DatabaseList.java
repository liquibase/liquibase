package liquibase.database;

import liquibase.util.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DatabaseList {
    /**
     * Compares a given database to a database definition string.
     * <p></p>
     * Definition syntax: Comma separated list of database shortNames. Doesn't matter if it includes spaces.
     * Can exclude a database by prepending its name with a '!'.
     * The string "all" will match all databases. The string "none" will match no databases, even if others are listed.
     * If an empty or null definition or null is passed, it will return the passed returnValueIfEmpty value.
     */
    public static boolean definitionMatches(String definition, String databaseShortName, boolean returnValueIfEmpty) {
        return definitionMatches(StringUtils.splitAndTrim(StringUtils.trimToNull(definition), ","), databaseShortName, returnValueIfEmpty);
    }

    public static boolean definitionMatches(String definition, Database database, boolean returnValueIfEmpty) {
        return definitionMatches(StringUtils.splitAndTrim(StringUtils.trimToNull(definition), ","), database, returnValueIfEmpty);
    }

    /**
     * Same logic as {@link #definitionMatches(String, liquibase.database.Database, boolean)} but with a collection of definitions rather than a comma separated list.
     */
    public static boolean definitionMatches(Collection<String> definition, String databaseShortName, boolean returnValueIfEmptyList) {
        if ((definition == null) || definition.isEmpty()) {
            return returnValueIfEmptyList;
        }

        if (definition.contains("all")) {
            return true;
        }

        if (definition.contains("none")) {
            return false;
        }

        // !h2 would mean that the h2 database should be excluded
        if (definition.contains("!" + databaseShortName)) {
            return false;
        }

        Set<String> dbmsSupported = new HashSet<>();
        // add all dbms that do not start with ! to a list
        for (String dbms: definition) {
            if (!dbms.startsWith("!")) {
                dbmsSupported.add(dbms);
            }
        }


        if (dbmsSupported.isEmpty() || dbmsSupported.contains(databaseShortName)) {
            return true;
        }

        return false;

    }

    public static boolean definitionMatches(Collection<String> definition, Database database, boolean returnValueIfEmptyList) {
        String shortName;
        if (database == null) {
            shortName = "null";
        } else {
            shortName = database.getShortName();
        }
        return definitionMatches(definition, shortName, returnValueIfEmptyList);
    }

    public static Set<String> toDbmsSet(String dbmsList) {
        Set<String> dbmsSet = null;
        if (StringUtils.trimToNull(dbmsList) != null) {
            dbmsSet = new HashSet<String>();
            for (String string : dbmsList.toLowerCase().split(",")) {
                dbmsSet.add(string.trim());
            }
        }
        return dbmsSet;
    }
}
